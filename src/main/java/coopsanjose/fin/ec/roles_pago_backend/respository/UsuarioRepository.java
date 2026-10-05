package coopsanjose.fin.ec.roles_pago_backend.respository;

import coopsanjose.fin.ec.roles_pago_backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsernameAd(String usernameAd);
    Optional<Usuario> findByCedula(String cedula);
    boolean existsByUsernameAd(String usernameAd);
}
