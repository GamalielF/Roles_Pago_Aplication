package coopsanjose.fin.ec.roles_pago_backend.respository;

import ec.fin.coopsanjose.rolespagobackend.entity.Rol;
import ec.fin.coopsanjose.rolespagobackend.entity.UsuarioRol;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, Long> {
    List<UsuarioRol> findByUsuario_IdUsuario(Long idUsuario);
    Optional<UsuarioRol> findByUsuario_IdUsuarioAndRol(Long idUsuario, Rol rol);
}
