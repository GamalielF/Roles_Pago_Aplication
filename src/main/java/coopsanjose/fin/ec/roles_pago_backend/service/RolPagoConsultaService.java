package coopsanjose.fin.ec.roles_pago_backend.service;

import coopsanjose.fin.ec.roles_pago_backend.dto.RolPagoContenidoDto;
import coopsanjose.fin.ec.roles_pago_backend.entity.*;
import coopsanjose.fin.ec.roles_pago_backend.repository.RolPagoArchivoRepository;
import coopsanjose.fin.ec.roles_pago_backend.storage.RolPagoStorageService;
import coopsanjose.fin.ec.roles_pago_backend.util.CedulaValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.List;

/**
 * SIN @Transactional A PROPOSITO: cuando se deniega un acceso lanzamos una
 * excepcion (403). Si todo el metodo fuera una sola transaccion, Spring haria
 * rollback y el registro ACCESO_DENEGADO de la auditoria se perderia justo
 * cuando mas importa. Asi, cada save() de auditoria se confirma por su cuenta.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RolPagoConsultaService {

    private final RolPagoArchivoRepository archivoRepository;
    private final RolPagoStorageService storageService;
    private final AuditoriaService auditoriaService;

    /** Periodos publicados que tiene esa cedula (para llenar un selector en Angular). */
    public List<String> periodosDisponibles(String cedula, Usuario usuario, String ip, String userAgent) {
        validarCedula(cedula);
        autorizar(cedula, usuario, ip, userAgent);
        return publicados(cedula).stream()
                .map(a -> YearMonth.from(a.getPeriodo().getFechaPeriodo()).toString())
                .toList();
    }

    /**
     * Devuelve el PDF en base64. Si no se indica periodo, entrega el mas reciente.
     * descarga=true cambia solo el tipo de evento auditado (DESCARGA_ROL vs CONSULTA_ROL).
     */
    public RolPagoContenidoDto consultar(String cedula, String periodoParam, boolean descarga,
                                         Usuario usuario, String ip, String userAgent) {
        validarCedula(cedula);
        Rol rolUtilizado = autorizar(cedula, usuario, ip, userAgent);

        List<RolPagoArchivo> disponibles = publicados(cedula);
        if (disponibles.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No hay roles de pago publicados para esa cedula");
        }

        RolPagoArchivo archivo;
        if (periodoParam == null || periodoParam.isBlank()) {
            archivo = disponibles.get(0);   // la consulta ordena de mas reciente a mas antiguo
        } else {
            YearMonth pedido = parsePeriodo(periodoParam);
            archivo = disponibles.stream()
                    .filter(a -> YearMonth.from(a.getPeriodo().getFechaPeriodo()).equals(pedido))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "No hay rol de pago publicado para el periodo " + pedido));
        }

        if (!storageService.existe(archivo.getRutaAlmacenamiento())) {
            log.error("Archivo registrado en BD pero ausente en disco: {}", archivo.getRutaAlmacenamiento());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "El archivo no esta disponible en el almacenamiento; contacte a RRHH");
        }
        byte[] contenido = storageService.leer(archivo.getRutaAlmacenamiento());

        auditoriaService.registrar(usuario,
                descarga ? TipoEvento.DESCARGA_ROL : TipoEvento.CONSULTA_ROL,
                rolUtilizado, cedula, archivo.getPeriodo(),
                ResultadoAuditoria.EXITOSO, ip, userAgent);

        YearMonth ym = YearMonth.from(archivo.getPeriodo().getFechaPeriodo());
        return new RolPagoContenidoDto(cedula, ym.toString(), "RolPago_" + ym + ".pdf",
                "application/pdf", contenido.length,
                Base64.getEncoder().encodeToString(contenido));
    }

    // ---------------- reglas de acceso ----------------

    /**
     * El corazon de la seguridad. Devuelve el ROL CON EL QUE actua el usuario:
     *  - Su propia cedula            -> EMPLEADO (aunque tambien sea RRHH o ADMIN)
     *  - Cedula ajena + rol RRHH     -> RRHH
     *  - Cedula ajena + rol ADMIN    -> ADMIN
     *  - Cualquier otro caso         -> se audita ACCESO_DENEGADO y se lanza 403
     * La validacion vive en el servidor: aunque Angular se manipule, no se salta.
     */
    private Rol autorizar(String cedula, Usuario usuario, String ip, String userAgent) {
        if (cedula.equals(usuario.getCedula())) return Rol.EMPLEADO;
        if (usuario.tieneRol(Rol.RRHH)) return Rol.RRHH;
        if (usuario.tieneRol(Rol.ADMIN)) return Rol.ADMIN;

        auditoriaService.registrar(usuario, TipoEvento.ACCESO_DENEGADO, Rol.EMPLEADO,
                cedula, null, ResultadoAuditoria.DENEGADO, ip, userAgent);

        String motivo = usuario.getCedula() == null
                ? "Su cedula aun no esta vinculada a su usuario"
                : "No tiene permiso para consultar esta cedula";
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, motivo);
    }

    // ---------------- utilidades ----------------

    private List<RolPagoArchivo> publicados(String cedula) {
        return archivoRepository.findPublicadosPorCedula(cedula, EstadoPeriodo.PUBLICADO);
    }

    private void validarCedula(String cedula) {
        if (!CedulaValidator.esValida(cedula)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cedula invalida");
        }
    }

    private YearMonth parsePeriodo(String periodo) {
        try {
            return YearMonth.parse(periodo);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Periodo invalido. Formato esperado: yyyy-MM (ej. 2026-08)");
        }
    }
}
