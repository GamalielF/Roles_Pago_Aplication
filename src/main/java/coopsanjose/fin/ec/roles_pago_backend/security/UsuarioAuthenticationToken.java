package coopsanjose.fin.ec.roles_pago_backend.security;

import coopsanjose.fin.ec.roles_pago_backend.entity.Usuario;
import coopsanjose.fin.ec.roles_pago_backend.entity.UsuarioRol;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Representa al usuario autenticado dentro de Spring Security. El "principal"
 * es la entidad Usuario completa (no solo un username), para que los
 * controladores puedan hacer @AuthenticationPrincipal Usuario usuario y
 * acceder directo a cedula, roles, etc.
 *
 * Es independiente de COMO se autentico (mock o JWT real): ambos filtros
 * construyen este mismo tipo de token, asi que el resto del sistema no
 * cambia cuando se reemplace el mock por el filtro definitivo.
 */
public class UsuarioAuthenticationToken extends AbstractAuthenticationToken {

    private final Usuario usuario;

    public UsuarioAuthenticationToken(Usuario usuario) {
        super(mapAuthorities(usuario));
        this.usuario = usuario;
        setAuthenticated(true);
    }

    private static List<GrantedAuthority> mapAuthorities(Usuario usuario) {
        return usuario.getRoles().stream()
                .map(UsuarioRol::getRol)
                .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.name()))
                .collect(Collectors.toList());
    }

    @Override
    public Object getCredentials() {
        return null; // no hay password que exponer; la identidad ya viene verificada
    }

    @Override
    public Object getPrincipal() {
        return usuario;
    }
}
