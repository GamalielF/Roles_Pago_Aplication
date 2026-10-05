package coopsanjose.fin.ec.roles_pago_backend.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.time.YearMonth;

@Service
public class FileSystemRolPagoStorageService implements RolPagoStorageService {

    private final Path base;

    public FileSystemRolPagoStorageService(
            @Value("${app.storage.roles-pago.base-path}") String basePath) throws IOException {
        this.base = Paths.get(basePath).toAbsolutePath().normalize();
        Files.createDirectories(this.base);
    }

    @Override
    public String guardar(YearMonth periodo, String cedula, byte[] contenido) {
        String relativa = periodo + "/" + cedula + ".pdf";   // YearMonth.toString() = "2026-08"
        Path destino = resolver(relativa);
        try {
            Files.createDirectories(destino.getParent());

            // Se escribe primero a un temporal y luego se renombra: si la
            // subida se corta a la mitad, nunca queda un PDF corrupto.
            Path temporal = destino.resolveSibling(cedula + ".pdf.tmp");
            Files.write(temporal, contenido);
            try {
                Files.move(temporal, destino,
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el archivo " + relativa, e);
        }
        return relativa;
    }

    @Override
    public byte[] leer(String rutaRelativa) {
        try {
            return Files.readAllBytes(resolver(rutaRelativa));
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el archivo " + rutaRelativa, e);
        }
    }

    @Override
    public boolean existe(String rutaRelativa) {
        return Files.exists(resolver(rutaRelativa));
    }

    /** Defensa contra path traversal: la ruta final debe quedar DENTRO de la carpeta base. */
    private Path resolver(String rutaRelativa) {
        Path resuelta = base.resolve(rutaRelativa).normalize();
        if (!resuelta.startsWith(base)) {
            throw new SecurityException("Ruta fuera del directorio de almacenamiento");
        }
        return resuelta;
    }
}
