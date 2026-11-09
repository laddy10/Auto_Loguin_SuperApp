package userinterfaces;

import io.appium.java_client.MobileBy;
import org.openqa.selenium.By;

/**
 * Elemento de pantalla para las comprobaciones RÁPIDAS (utils.Pantalla).
 *
 * A diferencia de los Target de Serenity, se busca directamente con el driver de
 * Appium y sin espera implícita: comprobar que algo NO está cuesta ~0,2 s en vez de
 * los ~10 s que tardaba Presence.of(target) (Serenity reimpone su propia espera
 * implícita de 10 s en cada búsqueda).
 *
 * Los localizadores son por id o por UiSelector (nunca XPath): el XPath obliga a
 * UiAutomator2 a volcar toda la pantalla y en Mi Claro eso tarda segundos.
 *
 * El nombre se usa en los mensajes de error y en el log, por eso va en español
 * y en el formato "el botón ...", "la pantalla ...".
 */
public final class Elemento {

    /** Paquete de la app Mi Claro. */
    public static final String PAQUETE = "com.clarocolombia.miclaro";

    private final String nombre;
    private final By localizador;

    private Elemento(String nombre, By localizador) {
        this.nombre = nombre;
        this.localizador = localizador;
    }

    public String nombre() {
        return nombre;
    }

    public By localizador() {
        return localizador;
    }

    @Override
    public String toString() {
        return nombre;
    }

    /** Por resource-id de la app (sin el prefijo del paquete) o completo ("android:id/button2"). */
    public static Elemento porId(String nombre, String id) {
        String completo = id.contains(":") ? id : PAQUETE + ":id/" + id;
        return new Elemento(nombre, By.id(completo));
    }

    /** Texto exacto, sin distinguir mayúsculas. */
    public static Elemento porTexto(String nombre, String texto) {
        return porUiSelector(nombre, "new UiSelector().textMatches(\"(?i)^" + regex(texto) + "$\")");
    }

    /** Texto que contiene el fragmento indicado (distingue mayúsculas, como UiSelector). */
    public static Elemento porTextoQueContiene(String nombre, String fragmento) {
        return porUiSelector(nombre, "new UiSelector().textContains(\"" + comillas(fragmento) + "\")");
    }

    /** Texto que cumple la expresión regular indicada (sintaxis de java.util.regex). */
    public static Elemento porTextoQueCumple(String nombre, String expresion) {
        return porUiSelector(nombre, "new UiSelector().textMatches(\"" + comillas(expresion) + "\")");
    }

    /** content-desc exacto. */
    public static Elemento porDescripcion(String nombre, String descripcion) {
        return porUiSelector(nombre, "new UiSelector().description(\"" + comillas(descripcion) + "\")");
    }

    public static Elemento porUiSelector(String nombre, String uiSelector) {
        return new Elemento(nombre, new MobileBy.ByAndroidUIAutomator(uiSelector));
    }

    /** Escapa los metacaracteres de regex para buscar el texto literal. */
    private static String regex(String texto) {
        StringBuilder sb = new StringBuilder();
        for (char c : texto.toCharArray()) {
            if ("\\.[]{}()*+-?^$|".indexOf(c) >= 0) {
                sb.append("\\\\");
            }
            sb.append(c);
        }
        return comillas(sb.toString());
    }

    private static String comillas(String texto) {
        return texto.replace("\"", "\\\"");
    }
}
