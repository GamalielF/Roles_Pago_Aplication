package coopsanjose.fin.ec.roles_pago_backend.service;

import coopsanjose.fin.ec.roles_pago_backend.dto.PublicacionDto;
import coopsanjose.fin.ec.roles_pago_backend.entity.EstadoPeriodo;
import coopsanjose.fin.ec.roles_pago_backend.entity.EstadoValidacion;
import coopsanjose.fin.ec.roles_pago_backend.entity.Periodo;
import coopsanjose.fin.ec.roles_pago_backend.entity.Usuario;
import coopsanjose.fin.ec.roles_pago_backend.repository.PeriodoRepository;
import coopsanjose.fin.ec.roles_pago_backend.repository.RolPagoArchivoRepository;
import coopsanjose.fin.ec.roles_pago_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class PeriodoService {

    private final PeriodoRepository periodoRepository;
    private final RolPagoArchivoRepository archivoRepository;
    private final UsuarioRepository usuarioRepository;

    /** Devuelve el periodo; si es la primera carga del mes, lo crea en estado CARGADO. */
    public Periodo obtenerOCrear(YearMonth ym) {
        LocalDate fecha = ym.atDay(1);   // convencion: siempre el dia 1 del mes
        return periodoRepository.findByFechaPeriodo(fecha).orElseGet(() ->
                periodoRepository.save(Periodo.builder()
                        .fechaPeriodo(fecha)
                        .estado(EstadoPeriodo.CARGADO)
                        .build()));
    }

    public Periodo obtener(YearMonth ym) {
        return periodoRepository.findByFechaPeriodo(ym.atDay(1)).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el periodo " + ym));
    }

    /**
     * Pasa el periodo de CARGADO a PUBLICADO. Desde ese momento los empleados
     * pueden consultar sus roles y el periodo ya no admite nuevas cargas.
     */
    @Transactional
    public PublicacionDto publicar(YearMonth ym, Usuario rrhh) {
        Periodo periodo = obtener(ym);

        if (periodo.getEstado() == EstadoPeriodo.PUBLICADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El periodo " + ym + " ya fue publicado el " + periodo.getFechaPublicacion());
        }

        long total = archivoRepository.countByPeriodo_IdPeriodo(periodo.getIdPeriodo());
        if (total == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede publicar: el periodo " + ym + " no tiene archivos cargados");
        }

        periodo.setEstado(EstadoPeriodo.PUBLICADO);
        periodo.setFechaPublicacion(LocalDateTime.now());
        periodo.setPublicadoPor(usuarioRepository.getReferenceById(rrhh.getIdUsuario()));
        periodoRepository.save(periodo);

        long advertencias = archivoRepository.countByPeriodo_IdPeriodoAndEstadoValidacion(
                periodo.getIdPeriodo(), EstadoValidacion.ADVERTENCIA);

        return new PublicacionDto(ym.toString(), periodo.getEstado(),
                periodo.getFechaPublicacion(), total, advertencias);
    }
}