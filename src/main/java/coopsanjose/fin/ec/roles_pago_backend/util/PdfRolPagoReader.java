package coopsanjose.fin.ec.roles_pago_backend.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.YearMonth;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lee el PDF y extrae el mes/anio de la cabecera ("AGOSTO 2026").
 * Sirve para detectar que RRHH no suba, por error, el rol de julio
 * dentro del periodo de agosto.
 */
@Component
public class PdfRolPagoReader {

    private static final Pattern MES_ANIO = Pattern.compile(
            "\\b(ENERO|FEBRERO|MARZO|ABRIL|MAYO|JUNIO|JULIO|AGOSTO|SEPTIEMBRE|OCTUBRE|NOVIEMBRE|DICIEMBRE)\\s+(\\d{4})\\b");

    /** @throws IOException si el PDF esta danado o no se puede leer */
    public Optional<YearMonth> leerPeriodo(byte[] pdf) throws IOException {
        try (PDDocument doc = Loader.loadPDF(pdf)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);   // el rol es de una sola pagina
            stripper.setEndPage(1);
            String texto = stripper.getText(doc).toUpperCase(Locale.ROOT);

            Matcher m = MES_ANIO.matcher(texto);
            if (m.find()) {
                return Optional.of(YearMonth.of(Integer.parseInt(m.group(2)), mesANumero(m.group(1))));
            }
            return Optional.empty();
        }
    }

    private static int mesANumero(String mes) {
        return switch (mes) {
            case "ENERO" -> 1;     case "FEBRERO" -> 2;    case "MARZO" -> 3;
            case "ABRIL" -> 4;     case "MAYO" -> 5;       case "JUNIO" -> 6;
            case "JULIO" -> 7;     case "AGOSTO" -> 8;     case "SEPTIEMBRE" -> 9;
            case "OCTUBRE" -> 10;  case "NOVIEMBRE" -> 11; case "DICIEMBRE" -> 12;
            default -> throw new IllegalArgumentException("Mes desconocido: " + mes);
        };
    }
}