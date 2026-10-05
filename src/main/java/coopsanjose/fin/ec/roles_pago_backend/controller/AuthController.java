package coopsanjose.fin.ec.roles_pago_backend.controller;

import coopsanjose.fin.ec.roles_pago_backend.dto.UsuarioLoginRequest;
import coopsanjose.fin.ec.roles_pago_backend.dto.UsuarioResponse;
import coopsanjose.fin.ec.roles_pago_backend.entity.Rol;
import coopsanjose.fin.ec.roles_pago_backend.entity.Usuario;
import coopsanjose.fin.ec.roles_pago_backend.entity.UsuarioRol;
import coopsanjose.fin.ec.roles_pago_backend.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    // TEMPORAL: simula lo que hará el filtro JWT más adelante.
    // Cuando tengamos el payload real, este endpoint se reemplaza por un
    // filtro que lee el JWT del header Authorization y llama a
    // usuarioService.resolverUsuario() con los datos extraídos del token.
    @PostMapping("/resolver")
    public UsuarioResponse resolver(@Valid @RequestBody UsuarioLoginRequest request,
                                    HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        return usuarioService.resolverUsuario(request, ip, userAgent);
    }
    // Agregar a AuthController.java

    @GetMapping("/me")
    public UsuarioResponse me(@AuthenticationPrincipal Usuario usuario) {
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "No autenticado (en este prototipo, falta la cabecera X-Mock-Username)");
        }
        List<Rol> roles = usuario.getRoles().stream().map(UsuarioRol::getRol).toList();
        return UsuarioResponse.builder()
                .idUsuario(usuario.getIdUsuario())
                .cedula(usuario.getCedula())
                .usernameAd(usuario.getUsernameAd())
                .email(usuario.getEmail())
                .nombreCompleto(usuario.getNombreCompleto())
                .roles(roles)
                .primerAcceso(false)
                .build();
    }
}



