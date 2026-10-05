package coopsanjose.fin.ec.roles_pago_backend.storage;

import java.time.YearMonth;

public interface RolPagoStorageService {
    /** Guarda el PDF y devuelve la ruta RELATIVA (ej. "2026-08/0202125712.pdf"). */
    String guardar(YearMonth periodo, String cedula, byte[] contenido);

    /** Lee un PDF a partir de su ruta relativa (lo usara la consulta del empleado). */
    byte[] leer(String rutaRelativa);

    boolean existe(String rutaRelativa);
}
