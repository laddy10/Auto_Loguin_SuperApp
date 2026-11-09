package utils;

/**
 * Datos de la cuenta de prueba tal como deben salir en el reporte: parcialmente
 * ocultos (como hace la propia app con "*******173"). La contraseña nunca se muestra.
 */
public final class Enmascarar {

    private Enmascarar() {
    }

    /** pruebasexperienciaclaro@gmail.com → pru***@gmail.com */
    public static String correo(String correo) {
        if (correo == null || !correo.contains("@")) {
            return "***";
        }
        int arroba = correo.indexOf('@');
        String usuario = correo.substring(0, arroba);
        String visible = usuario.length() <= 3 ? usuario.substring(0, 1) : usuario.substring(0, 3);
        return visible + "***" + correo.substring(arroba);
    }

    /** 3222787173 → 322****173 */
    public static String celular(String numero) {
        if (numero == null || numero.length() < 7) {
            return "***";
        }
        return numero.substring(0, 3) + "****" + numero.substring(numero.length() - 3);
    }

    /** 99321987 → ****1987 */
    public static String documento(String documento) {
        if (documento == null || documento.length() < 5) {
            return "***";
        }
        return "****" + documento.substring(documento.length() - 4);
    }
}
