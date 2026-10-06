package coopsanjose.fin.ec.roles_pago_backend.dto;

/** El PDF viaja como base64 dentro del JSON; Angular lo decodifica a Blob. */
public record RolPagoContenidoDto(String cedula, String periodo, String nombreArchivo,
                                  String mimeType, long tamanoBytes, String contenidoBase64) {}
