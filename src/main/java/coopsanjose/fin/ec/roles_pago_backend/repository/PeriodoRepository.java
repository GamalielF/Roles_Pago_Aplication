package coopsanjose.fin.ec.roles_pago_backend.repository;

import coopsanjose.fin.ec.roles_pago_backend.entity.Periodo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface PeriodoRepository extends JpaRepository<Periodo, Long> {
    Optional<Periodo> findByFechaPeriodo(LocalDate fechaPeriodo);
}
