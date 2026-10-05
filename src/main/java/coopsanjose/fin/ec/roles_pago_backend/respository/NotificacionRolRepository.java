package coopsanjose.fin.ec.roles_pago_backend.respository;

import coopsanjose.fin.ec.roles_pago_backend.entity.EstadoNotificacion;
import coopsanjose.fin.ec.roles_pago_backend.entity.NotificacionRol;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificacionRolRepository extends JpaRepository<NotificacionRol, Long> {
    List<NotificacionRol> findByEstado(EstadoNotificacion estado);
}
