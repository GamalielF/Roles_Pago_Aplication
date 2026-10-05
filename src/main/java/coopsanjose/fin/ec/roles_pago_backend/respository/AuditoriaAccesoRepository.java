package coopsanjose.fin.ec.roles_pago_backend.respository;

import coopsanjose.fin.ec.roles_pago_backend.entity.AuditoriaAcceso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditoriaAccesoRepository extends JpaRepository<AuditoriaAcceso, Long> {
    List<AuditoriaAcceso> findByUsuario_IdUsuarioOrderByFechaHoraDesc(Long idUsuario);
}
