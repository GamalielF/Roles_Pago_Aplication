package coopsanjose.fin.ec.roles_pago_backend.service;

import coopsanjose.fin.ec.roles_pago_backend.entity.EstadoPeriodo;
import coopsanjose.fin.ec.roles_pago_backend.entity.Periodo;
import coopsanjose.fin.ec.roles_pago_backend.repository.PeriodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class PeriodoService {

    private final PeriodoRepository periodoRepository;

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
}
