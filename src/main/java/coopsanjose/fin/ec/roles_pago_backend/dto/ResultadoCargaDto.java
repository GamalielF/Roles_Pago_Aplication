package coopsanjose.fin.ec.roles_pago_backend.dto;

import java.util.List;

public record ResultadoCargaDto(String periodo, int total, int cargados,
                                int conAdvertencia, int rechazados,
                                List<ResultadoArchivoDto> resultados) {}
