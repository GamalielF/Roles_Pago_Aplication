package coopsanjose.fin.ec.roles_pago_backend.dto;

import coopsanjose.fin.ec.roles_pago_backend.entity.EstadoPeriodo;
import java.time.LocalDateTime;

public record PublicacionDto(String periodo, EstadoPeriodo estado, LocalDateTime fechaPublicacion,
                             long totalArchivos, long conAdvertencia) {}