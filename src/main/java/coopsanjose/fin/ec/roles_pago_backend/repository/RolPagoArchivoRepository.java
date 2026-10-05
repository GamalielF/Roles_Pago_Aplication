package coopsanjose.fin.ec.roles_pago_backend.repository;

import coopsanjose.fin.ec.roles_pago_backend.entity.RolPagoArchivo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RolPagoArchivoRepository extends JpaRepository<RolPagoArchivo, Long> {
    Optional<RolPagoArchivo> findByCedulaAndPeriodo_IdPeriodo(String cedula, Long idPeriodo);
    List<RolPagoArchivo> findByPeriodo_IdPeriodo(Long idPeriodo);
    boolean existsByCedulaAndPeriodo_IdPeriodo(String cedula, Long idPeriodo);
}