package coopsanjose.fin.ec.roles_pago_backend.service;

import coopsanjose.fin.ec.roles_pago_backend.dto.ArchivoResumenDto;
import coopsanjose.fin.ec.roles_pago_backend.dto.ResultadoArchivoDto;
import coopsanjose.fin.ec.roles_pago_backend.dto.ResultadoCargaDto;
import coopsanjose.fin.ec.roles_pago_backend.entity.*;
import coopsanjose.fin.ec.roles_pago_backend.repository.RolPagoArchivoRepository;
import coopsanjose.fin.ec.roles_pago_backend.repository.UsuarioRepository;
import coopsanjose.fin.ec.roles_pago_backend.storage.RolPagoStorageService;
import coopsanjose.fin.ec.roles_pago_backend.util.CedulaValidator;
import coopsanjose.fin.ec.roles_pago_backend.util.PdfRolPagoReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class RolPagoCargaService {

    // El nombre del archivo ES la cedula: 0202125712.pdf
    private static final Pattern NOMBRE_ARCHIVO =
            Pattern.compile("^(\\d{10})\\.pdf$", Pattern.CASE_INSENSITIVE);

    private final PeriodoService periodoService;
    private final RolPagoArchivoRepository archivoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolPagoStorageService storageService;
    private final PdfRolPagoReader pdfReader;

    @Value("${app.storage.roles-pago.max-file-bytes:5242880}")
    private long maxBytes;

    // Sin @Transactional a proposito: cada save() del repositorio es su propia
    // transaccion, asi un archivo con problemas no revierte los que ya se cargaron.
    public ResultadoCargaDto procesarLote(YearMonth ym, List<MultipartFile> archivos, Usuario rrhh) {

        Periodo periodo = periodoService.obtenerOCrear(ym);
        if (periodo.getEstado() == EstadoPeriodo.PUBLICADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El periodo " + ym + " ya fue publicado y no admite nuevas cargas");
        }

        // getReferenceById: solo necesitamos el id para la FK subido_por (evita un SELECT)
        Usuario subidoPor = usuarioRepository.getReferenceById(rrhh.getIdUsuario());

        List<ResultadoArchivoDto> resultados = new ArrayList<>();
        for (MultipartFile archivo : archivos) {
            resultados.add(procesarArchivo(ym, periodo, subidoPor, archivo));
        }

        int rechazados = (int) resultados.stream().filter(r -> r.estado() == EstadoValidacion.ERROR).count();
        int advertencias = (int) resultados.stream().filter(r -> r.estado() == EstadoValidacion.ADVERTENCIA).count();

        return new ResultadoCargaDto(ym.toString(), resultados.size(),
                resultados.size() - rechazados, advertencias, rechazados, resultados);
    }

    private ResultadoArchivoDto procesarArchivo(YearMonth ym, Periodo periodo,
                                                Usuario subidoPor, MultipartFile file) {
        String nombre = nombreLimpio(file.getOriginalFilename());
        try {
            // 1. Formato del nombre: <10 digitos>.pdf
            Matcher m = NOMBRE_ARCHIVO.matcher(nombre);
            if (!m.matches()) {
                return rechazo(nombre, null, "Nombre invalido: debe ser <cedula de 10 digitos>.pdf");
            }
            String cedula = m.group(1);

            // 2. Cedula real (digito verificador)
            if (!CedulaValidator.esValida(cedula)) {
                return rechazo(nombre, cedula, "Cedula invalida (no pasa el digito verificador)");
            }

            // 3. Tamano
            if (file.isEmpty()) return rechazo(nombre, cedula, "El archivo esta vacio");
            if (file.getSize() > maxBytes) {
                return rechazo(nombre, cedula, "Supera el tamano maximo de " + (maxBytes / 1024 / 1024) + " MB");
            }

            // 4. Que sea un PDF de verdad (cabecera "%PDF-"), no solo la extension
            byte[] contenido = file.getBytes();
            if (!esPdf(contenido)) {
                return rechazo(nombre, cedula, "El contenido no es un PDF valido");
            }

            // 5. Mes/anio dentro del PDF vs periodo elegido por RRHH
            EstadoValidacion estado = EstadoValidacion.OK;
            String detalle = null;
            YearMonth mesPdf = null;
            try {
                Optional<YearMonth> leido = pdfReader.leerPeriodo(contenido);
                if (leido.isEmpty()) {
                    estado = EstadoValidacion.ADVERTENCIA;
                    detalle = "No se encontro mes/anio dentro del PDF; verifique manualmente";
                } else {
                    mesPdf = leido.get();
                    if (!mesPdf.equals(ym)) {
                        return rechazo(nombre, cedula,
                                "El PDF corresponde a " + mesPdf + " pero se esta cargando el periodo " + ym);
                    }
                }
            } catch (IOException e) {
                return rechazo(nombre, cedula, "PDF ilegible o danado");
            }

            // 6. Duplicados: mismo contenido => no se toca; distinto => se reemplaza
            String hash = sha256(contenido);
            Optional<RolPagoArchivo> existente =
                    archivoRepository.findByCedulaAndPeriodo_IdPeriodo(cedula, periodo.getIdPeriodo());

            if (existente.isPresent() && hash.equals(existente.get().getHashArchivo())) {
                return new ResultadoArchivoDto(nombre, cedula, existente.get().getEstadoValidacion(),
                        "SIN_CAMBIOS", "Ya existia un archivo identico; no se modifico nada");
            }

            // 7. Guardar en disco y registrar metadatos en BD
            String ruta = storageService.guardar(ym, cedula, contenido);

            RolPagoArchivo entidad = existente.orElseGet(RolPagoArchivo::new);
            entidad.setPeriodo(periodo);
            entidad.setSubidoPor(subidoPor);
            entidad.setCedula(cedula);
            entidad.setNombreArchivoOriginal(nombre);
            entidad.setRutaAlmacenamiento(ruta);
            entidad.setHashArchivo(hash);
            entidad.setTamanoBytes((long) contenido.length);
            entidad.setMesExtraidoPdf(mesPdf != null ? mesPdf.toString() : null);
            entidad.setEstadoValidacion(estado);
            entidad.setDetalleValidacion(detalle);
            archivoRepository.save(entidad);

            return new ResultadoArchivoDto(nombre, cedula, estado,
                    existente.isPresent() ? "REEMPLAZADO" : "CREADO",
                    detalle != null ? detalle : "Cargado correctamente");

        } catch (Exception e) {
            log.error("Error procesando el archivo {}", nombre, e);
            return rechazo(nombre, null, "Error interno al procesar el archivo");
        }
    }

    public List<ArchivoResumenDto> listar(YearMonth ym) {
        Periodo periodo = periodoService.obtener(ym);
        return archivoRepository.findByPeriodo_IdPeriodo(periodo.getIdPeriodo()).stream()
                .map(a -> new ArchivoResumenDto(a.getCedula(), a.getNombreArchivoOriginal(),
                        a.getEstadoValidacion(), a.getDetalleValidacion(),
                        a.getTamanoBytes(), a.getFechaSubida()))
                .toList();
    }

    // ---------- utilidades privadas ----------

    private ResultadoArchivoDto rechazo(String archivo, String cedula, String mensaje) {
        return new ResultadoArchivoDto(archivo, cedula, EstadoValidacion.ERROR, "RECHAZADO", mensaje);
    }

    /** Algunos navegadores envian la ruta completa; nos quedamos solo con el nombre. */
    private String nombreLimpio(String original) {
        if (original == null) return "";
        String n = original.replace("\\", "/");
        return n.substring(n.lastIndexOf('/') + 1).trim();
    }

    private boolean esPdf(byte[] bytes) {
        return bytes.length >= 5
                && new String(bytes, 0, 5, StandardCharsets.ISO_8859_1).equals("%PDF-");
    }

    private String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
