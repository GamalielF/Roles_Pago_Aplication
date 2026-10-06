package coopsanjose.fin.ec.roles_pago_backend.repository;

import coopsanjose.fin.ec.roles_pago_backend.entity.EstadoPeriodo;
import coopsanjose.fin.ec.roles_pago_backend.entity.EstadoValidacion;
import coopsanjose.fin.ec.roles_pago_backend.entity.RolPagoArchivo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface RolPagoArchivoRepository extends JpaRepository<RolPagoArchivo, Long> {
    long countByPeriodo_IdPeriodo(Long idPeriodo);
    long countByPeriodo_IdPeriodoAndEstadoValidacion(Long idPeriodo, EstadoValidacion estado);
    @Query("select a from RolPagoArchivo a join fetch a.periodo p " +
            "where a.cedula = :cedula and p.estado = :estado " +
            "order by p.fechaPeriodo desc")
    List<RolPagoArchivo> findPublicadosPorCedula(@Param("cedula") String cedula,
                                                 @Param("estado") EstadoPeriodo estado);
    Optional<RolPagoArchivo> findByCedulaAndPeriodo_IdPeriodo(String cedula, Long idPeriodo);
    List<RolPagoArchivo> findByPeriodo_IdPeriodo(Long idPeriodo);
    boolean existsByCedulaAndPeriodo_IdPeriodo(String cedula, Long idPeriodo);
}