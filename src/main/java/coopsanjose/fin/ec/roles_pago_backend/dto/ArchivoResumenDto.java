package coopsanjose.fin.ec.roles_pago_backend.dto;

import coopsanjose.fin.ec.roles_pago_backend.entity.EstadoValidacion;
import java.time.LocalDateTime;

public record ArchivoResumenDto(String cedula, String nombreOriginal, EstadoValidacion estado,
                                String detalle, Long tamanoBytes, LocalDateTime fechaSubida) {}