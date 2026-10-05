package coopsanjose.fin.ec.roles_pago_backend.security;

import coopsanjose.fin.ec.roles_pago_backend.dto.UsuarioLoginRequest;
import coopsanjose.fin.ec.roles_pago_backend.entity.Usuario;
import coopsanjose.fin.ec.roles_pago_backend.service.UsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ============================================================================
 *  FILTRO TEMPORAL DE PROTOTIPO - NO USAR EN PRODUCCION
 * ============================================================================
 * Simula la identidad del usuario autenticado leyendo cabeceras HTTP, mientras
 * el equipo de Auth no entrega el JWT real. Internamente llama exactamente al
 * mismo UsuarioService que usara el filtro JWT definitivo, asi que toda la
 * logica de "primer acceso" / auditoria / rol EMPLEADO automatico ya queda
 * probada de antemano.
 *
 * Cabeceras que reconoce (enviar desde Postman o Angular en modo dev):
 *   X-Mock-Username      (obligatoria)
 *   X-Mock-Email
 *   X-Mock-Nombre
 *   X-Mock-Cargo
 *   X-Mock-Departamento
 *   X-Mock-Cedula        (opcional)
 *
 * CUANDO EL JWT REAL ESTE LISTO:
 *   1. Deja de registrar este filtro en SecurityConfig.
 *   2. Sigue las instrucciones dentro de JwtAuthFilter.java.
 * ============================================================================
 */
@RequiredArgsConstructor
public class MockAuthFilter extends OncePerRequestFilter {

    private final UsuarioService usuarioService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String username = request.getHeader("X-Mock-Username");

        if (username != null && !username.isBlank()) {
            UsuarioLoginRequest loginRequest = new UsuarioLoginRequest();
            loginRequest.setUsernameAd(username);
            loginRequest.setEmail(request.getHeader("X-Mock-Email"));
            loginRequest.setNombreCompleto(request.getHeader("X-Mock-Nombre"));
            loginRequest.setCargo(request.getHeader("X-Mock-Cargo"));
            loginRequest.setDepartamento(request.getHeader("X-Mock-Departamento"));
            loginRequest.setCedula(request.getHeader("X-Mock-Cedula"));

            String ip = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            Usuario usuario = usuarioService.resolverUsuarioEntity(loginRequest, ip, userAgent);

            SecurityContextHolder.getContext()
                    .setAuthentication(new UsuarioAuthenticationToken(usuario));
        }

        filterChain.doFilter(request, response);
    }
}