package coopsanjose.fin.ec.roles_pago_backend.dto;

import coopsanjose.fin.ec.roles_pago_backend.entity.EstadoValidacion;

/** Resultado de UN archivo del lote. accion: CREADO | REEMPLAZADO | SIN_CAMBIOS | RECHAZADO */
public record ResultadoArchivoDto(String archivo, String cedula, EstadoValidacion estado,
                                  String accion, String mensaje) {}
