package coopsanjose.fin.ec.roles_pago_backend.config;

import coopsanjose.fin.ec.roles_pago_backend.security.MockAuthFilter;
import coopsanjose.fin.ec.roles_pago_backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final UsuarioService usuarioService;

    // Bandera de seguridad: el filtro mock SOLO se registra si esta en true.
    // Si alguien despliega sin esta propiedad, queda apagado (no abierto).
    @Value("${app.security.mock-enabled:false}")
    private boolean mockEnabled;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ORDEN IMPORTANTE: primero las reglas especificas...
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/rrhh/**").hasAnyRole("RRHH", "ADMIN")
                        // ...y al FINAL la regla general. Despues de anyRequest()
                        // ya no se puede agregar ningun requestMatchers.
                        .anyRequest().permitAll()
                );

        // PROTOTIPO -> filtro mock (cabeceras X-Mock-*)
        // PRODUCCION -> reemplazar por: new JwtAuthFilter(usuarioService)
        if (mockEnabled) {
            http.addFilterBefore(new MockAuthFilter(usuarioService),
                    UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }
}