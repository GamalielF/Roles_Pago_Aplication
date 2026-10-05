package coopsanjose.fin.ec.roles_pago_backend.util;

/**
 * Valida una cedula ecuatoriana con el algoritmo modulo 10.
 * Un nombre de archivo como 1234567890.pdf pasaria el formato de 10 digitos,
 * pero no es una cedula real: esto lo detecta antes de guardar nada.
 */
public final class CedulaValidator {

    private CedulaValidator() {}

    public static boolean esValida(String cedula) {
        if (cedula == null || !cedula.matches("\\d{10}")) return false;

        // Provincia: 01-24 (o 30 para ecuatorianos en el exterior)
        int provincia = Integer.parseInt(cedula.substring(0, 2));
        if (!((provincia >= 1 && provincia <= 24) || provincia == 30)) return false;

        // Tercer digito < 6 para personas naturales
        if (cedula.charAt(2) - '0' >= 6) return false;

        // Coeficientes 2,1,2,1,2,1,2,1,2 sobre los 9 primeros digitos
        int suma = 0;
        for (int i = 0; i < 9; i++) {
            int digito = cedula.charAt(i) - '0';
            int producto = (i % 2 == 0) ? digito * 2 : digito;
            if (producto >= 10) producto -= 9;
            suma += producto;
        }
        int verificador = (10 - (suma % 10)) % 10;
        return verificador == (cedula.charAt(9) - '0');
    }
}
