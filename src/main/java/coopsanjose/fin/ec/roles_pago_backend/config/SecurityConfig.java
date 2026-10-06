package coopsanjose.fin.ec.roles_pago_backend.config;

import coopsanjose.fin.ec.roles_pago_backend.security.MockAuthFilter;
import coopsanjose.fin.ec.roles_pago_backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final UsuarioService usuarioService;

    @Value("${app.security.mock-enabled:false}")
    private boolean mockEnabled;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Sin identidad -> 401 (el frontend redirige al login).
                // Con identidad pero sin permiso -> 403.
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        // De lo mas especifico a lo mas general; anyRequest() SIEMPRE al final.
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/rrhh/**").hasAnyRole("RRHH", "ADMIN")
                        .requestMatchers("/api/roles-pago/**").authenticated()
                        .anyRequest().permitAll()
                );

        if (mockEnabled) {
            http.addFilterBefore(new MockAuthFilter(usuarioService),
                    UsernamePasswordAuthenticationFilter.class);
        }
        return http.build();
    }
}