package coopsanjose.fin.ec.roles_pago_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsuarioLoginRequest {
    @NotBlank
    private String usernameAd;
    private String email;
    private String nombreCompleto;
    private String cargo;
    private String departamento;
    private String cedula; // opcional por ahora
}
