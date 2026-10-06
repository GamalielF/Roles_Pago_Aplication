package coopsanjose.fin.ec.roles_pago_backend.repository;

import coopsanjose.fin.ec.roles_pago_backend.entity.AuditoriaAcceso;
import coopsanjose.fin.ec.roles_pago_backend.entity.TipoEvento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface AuditoriaAccesoRepository extends JpaRepository<AuditoriaAcceso, Long> {
    List<AuditoriaAcceso> findByUsuario_IdUsuarioOrderByFechaHoraDesc(Long idUsuario);
    boolean existsByUsuario_IdUsuarioAndTipoEventoInAndFechaHoraAfter(
            Long idUsuario, Collection<TipoEvento> tipos, LocalDateTime desde);
}
