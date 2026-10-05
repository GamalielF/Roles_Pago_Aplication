package coopsanjose.fin.ec.roles_pago_backend.dto;

import coopsanjose.fin.ec.roles_pago_backend.entity.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class UsuarioResponse {
    private Long idUsuario;
    private String cedula;
    private String usernameAd;
    private String email;
    private String nombreCompleto;
    private List<Rol> roles;
    private boolean primerAcceso;
}
