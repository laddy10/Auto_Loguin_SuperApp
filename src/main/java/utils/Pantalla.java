package utils;

import io.appium.java_client.MobileBy;
import io.appium.java_client.android.AndroidDriver;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.thucydides.core.webdriver.WebDriverFacade;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import userinterfaces.Elemento;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Interacción RÁPIDA con la pantalla, directo contra el driver de Appium.
 *
 * Por qué existe:
 *  1. Velocidad. Las comprobaciones con Serenity (Presence.of / WaitUntil) pagan la
 *     espera implícita de 10 s cada vez que el elemento NO está. Un flujo que revisaba
 *     6 avisos opcionales perdía un minuto aunque ninguno apareciera. Aquí se busca sin
 *     espera implícita: comprobar que algo no está cuesta ~0,2 s.
 *  2. Reporte limpio. Las acciones de Screenplay (Click.on, Enter.theValue...) se
 *     registran como pasos "actor clicks on..." e incluso escriben la contraseña en el
 *     reporte. Estas acciones no generan pasos: los pasos los define utils.Paso, en
 *     español y con lo que el negocio entiende.
 *
 * Estrategia "camino feliz primero" (ver {@link #esperarAlguno}): se espera la pantalla
 * que el flujo normal debería mostrar y SOLO si no aparece en un momento se revisan los
 * desvíos conocidos (sesión abierta en otro dispositivo, versión Empresas, etc.).
 */
public final class Pantalla {

    /** Cada cuánto se vuelve a mirar la pantalla mientras se espera. */
    private static final long SONDEO_MS = 300;
    /** Tiempo que se le da al flujo normal antes de empezar a revisar desvíos. */
    private static final long GRACIA_DESVIOS_MS = 1500;
    /** Pausa entre que aparece un elemento y se pulsa (fin de la animación de entrada). */
    private static final long ASENTAR_MS = 400;
    /** Patrones de textos del sistema que no aportan al describir la pantalla. */
    private static final String RUIDO = "^(\\d{1,2}:\\d{2}|\\d{1,3}\\s?%?|[\\d,.]+\\s?(KB|MB|K/s|B/s).*)$";

    private Pantalla() {
    }

    /** Driver de Appium real (sin la capa de Serenity, que impone su espera implícita). */
    @SuppressWarnings("unchecked")
    public static AndroidDriver<WebElement> driver(Actor actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        if (driver instanceof WebDriverFacade) {
            driver = ((WebDriverFacade) driver).getProxiedDriver();
        }
        return (AndroidDriver<WebElement>) driver;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Comprobaciones instantáneas
    // ═════════════════════════════════════════════════════════════════════════

    /** ¿Está el elemento en pantalla en este momento? No espera nada. */
    public static boolean esta(Actor actor, Elemento elemento) {
        return !buscar(actor, elemento).isEmpty();
    }

    private static List<WebElement> buscar(Actor actor, Elemento elemento) {
        AndroidDriver<WebElement> driver = driver(actor);
        try {
            return driver.findElements(elemento.localizador());
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return Collections.emptyList();
        } catch (WebDriverException e) {
            // Un parpadeo de UiAutomator2 no debe tumbar el flujo: se reintenta una vez.
            // Si vuelve a fallar se propaga: es la sesión de Appium la que está mal y
            // esperar no lo va a arreglar.
            dormir(500);
            try {
                return driver.findElements(elemento.localizador());
            } catch (NoSuchElementException | StaleElementReferenceException e2) {
                return Collections.emptyList();
            }
        }
    }

    /**
     * Quita la espera implícita del driver. Se llama al inicio de cada operación (no en
     * cada búsqueda) para no pagar una llamada extra a Appium por comprobación.
     */
    private static void sinEsperaImplicita(Actor actor) {
        try {
            driver(actor).manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
        } catch (WebDriverException ignored) {
            // Si no se pudo, la siguiente búsqueda lo dirá.
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Esperas
    // ═════════════════════════════════════════════════════════════════════════

    /** Espera a que aparezca el elemento, atendiendo los desvíos si no aparece pronto. */
    public static boolean esperar(Actor actor, Elemento esperado, int segundos, Desvio... desvios) {
        return esperarAlguno(actor, segundos, Collections.singletonList(esperado), desvios) != null;
    }

    /**
     * Espera a que aparezca CUALQUIERA de los elementos esperados y devuelve el primero
     * que aparece (null si se acaba el tiempo).
     *
     * Primero se mira solo el camino feliz. Si tras {@value #GRACIA_DESVIOS_MS} ms no ha
     * aparecido, en cada vuelta se revisan también los desvíos y se atiende el primero
     * que esté en pantalla (cada uno como máximo las veces que permita). Después de
     * atender uno se vuelve a mirar el camino feliz de inmediato.
     */
    public static Elemento esperarAlguno(Actor actor, int segundos, List<Elemento> esperados,
                                         Desvio... desvios) {
        sinEsperaImplicita(actor);
        long inicio = System.currentTimeMillis();
        long limite = inicio + segundos * 1000L;
        List<Desvio> pendientes = new ArrayList<>(Arrays.asList(desvios));
        int[] usos = new int[pendientes.size()];

        while (true) {
            for (Elemento esperado : esperados) {
                if (esta(actor, esperado)) {
                    return esperado;
                }
            }
            long ahora = System.currentTimeMillis();
            if (ahora >= limite) {
                return null;
            }
            if (ahora - inicio >= GRACIA_DESVIOS_MS) {
                for (int i = 0; i < pendientes.size(); i++) {
                    Desvio desvio = pendientes.get(i);
                    if (usos[i] < desvio.maximo && desvio.estaPresente(actor)) {
                        usos[i]++;
                        desvio.atender(actor);
                        sinEsperaImplicita(actor);
                        break;
                    }
                }
            }
            dormir(SONDEO_MS);
        }
    }

    /** Espera a que el elemento deje de estar en pantalla. */
    public static boolean esperarQueDesaparezca(Actor actor, Elemento elemento, int segundos) {
        sinEsperaImplicita(actor);
        long limite = System.currentTimeMillis() + segundos * 1000L;
        while (System.currentTimeMillis() < limite) {
            if (!esta(actor, elemento)) {
                return true;
            }
            dormir(SONDEO_MS);
        }
        return !esta(actor, elemento);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Acciones (ninguna genera pasos en el reporte)
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Espera a que el elemento aparezca y lo pulsa. Si no aparece, falla explicando qué faltó.
     *
     * Antes de pulsar deja {@value #ASENTAR_MS} ms para que termine la animación de entrada
     * (paneles que suben, diálogos que aparecen): pulsar a mitad de la animación cae en
     * otra coordenada. El flujo anterior esperaba 1 s fijo antes de cada clic por texto.
     */
    public static void tocar(Actor actor, Elemento elemento, int segundos, Desvio... desvios) {
        if (!esperar(actor, elemento, segundos, desvios)) {
            throw noAparecio(actor, elemento, segundos);
        }
        dormir(ASENTAR_MS);
        primero(actor, elemento).click();
    }

    /** Pulsa el elemento solo si está en pantalla en este momento. */
    public static boolean tocarSiEsta(Actor actor, Elemento elemento) {
        sinEsperaImplicita(actor);
        List<WebElement> encontrados = buscar(actor, elemento);
        if (encontrados.isEmpty()) {
            return false;
        }
        try {
            encontrados.get(0).click();
            return true;
        } catch (StaleElementReferenceException | NoSuchElementException e) {
            return false;
        }
    }

    /** Espera el campo, lo pulsa, lo limpia y escribe el valor. No registra el valor en ninguna parte. */
    public static void escribir(Actor actor, Elemento campo, String valor, int segundos) {
        if (!esperar(actor, campo, segundos)) {
            throw noAparecio(actor, campo, segundos);
        }
        WebElement elemento = primero(actor, campo);
        elemento.click();
        elemento.clear();
        elemento.sendKeys(valor);
    }

    /**
     * Cierra el teclado con "atrás" (como hacía el flujo original), pero solo si el
     * teclado está abierto: un "atrás" sin teclado sacaría a la app de la pantalla.
     */
    public static void ocultarTeclado(Actor actor) {
        boolean abierto;
        try {
            abierto = driver(actor).isKeyboardShown();
        } catch (WebDriverException e) {
            abierto = true; // no se pudo saber: comportamiento original
        }
        if (abierto) {
            driver(actor).navigate().back();
            dormir(500);
        }
    }

    /** Texto del elemento si está en pantalla en este momento; null si no está. */
    public static String textoSiEsta(Actor actor, Elemento elemento) {
        List<WebElement> encontrados = buscar(actor, elemento);
        if (encontrados.isEmpty()) {
            return null;
        }
        try {
            String texto = encontrados.get(0).getText();
            return texto == null ? "" : texto;
        } catch (StaleElementReferenceException | NoSuchElementException e) {
            return null;
        }
    }

    /** Texto del elemento (esperándolo hasta los segundos indicados). */
    public static String texto(Actor actor, Elemento elemento, int segundos) {
        if (!esperar(actor, elemento, segundos)) {
            throw noAparecio(actor, elemento, segundos);
        }
        String texto = primero(actor, elemento).getText();
        return texto == null ? "" : texto.trim();
    }

    private static WebElement primero(Actor actor, Elemento elemento) {
        List<WebElement> encontrados = buscar(actor, elemento);
        if (encontrados.isEmpty()) {
            throw new AssertionError("Desapareció " + elemento.nombre() + " antes de poder usarlo."
                    + describirPantalla(actor));
        }
        return encontrados.get(0);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Diagnóstico
    // ═════════════════════════════════════════════════════════════════════════

    /** Error estándar cuando algo esperado no aparece: incluye lo que sí se ve en pantalla. */
    public static AssertionError noAparecio(Actor actor, Elemento elemento, int segundos) {
        return new AssertionError("No apareció " + elemento.nombre() + " en " + segundos + " s."
                + describirPantalla(actor));
    }

    /**
     * Resume en una frase los textos visibles (" En pantalla: «a», «b»."), para que el
     * reporte y la IA de diagnóstico sepan dónde se quedó la app. Nunca falla.
     */
    public static String describirPantalla(Actor actor) {
        try {
            sinEsperaImplicita(actor);
            List<WebElement> conTexto = driver(actor).findElements(
                    new MobileBy.ByAndroidUIAutomator("new UiSelector().textMatches(\".+\")"));
            Set<String> textos = new LinkedHashSet<>();
            for (WebElement e : conTexto) {
                if (textos.size() >= 8) {
                    break;
                }
                String t = e.getText();
                if (t == null) {
                    continue;
                }
                t = t.replaceAll("\\s+", " ").trim();
                if (t.isEmpty() || t.matches(RUIDO)) {
                    continue;
                }
                textos.add(t.length() > 80 ? t.substring(0, 77) + "..." : t);
            }
            if (textos.isEmpty()) {
                return "";
            }
            return " En pantalla: «" + String.join("», «", textos) + "».";
        } catch (Throwable t) {
            return "";
        }
    }

    /** Pausa corta. Respeta la interrupción del hilo. */
    public static void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Desvíos
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Pantalla que a veces interrumpe el flujo normal y la forma de salir de ella.
     * Al atenderse queda registrada como un paso del reporte ("Se cerró ...") para que
     * se entienda por qué el flujo tomó otro camino.
     */
    public static final class Desvio {
        private final String descripcion;
        private final Predicate<Actor> detector;
        private final Consumer<Actor> accion;
        private final int maximo;

        private Desvio(String descripcion, Predicate<Actor> detector, Consumer<Actor> accion, int maximo) {
            this.descripcion = descripcion;
            this.detector = detector;
            this.accion = accion;
            this.maximo = maximo;
        }

        /**
         * @param elemento    lo que delata el desvío
         * @param descripcion cómo se resuelve, tal como saldrá en el reporte
         * @param accion      qué hacer para volver al flujo normal
         */
        public static Desvio si(Elemento elemento, String descripcion, Consumer<Actor> accion) {
            return new Desvio(descripcion, a -> esta(a, elemento), accion, 2);
        }

        public static Desvio si(Predicate<Actor> detector, String descripcion, Consumer<Actor> accion) {
            return new Desvio(descripcion, detector, accion, 2);
        }

        /** Cuántas veces como máximo se atiende durante una misma espera (por defecto 2). */
        public Desvio maximo(int veces) {
            return new Desvio(descripcion, detector, accion, veces);
        }

        boolean estaPresente(Actor actor) {
            return detector.test(actor);
        }

        void atender(Actor actor) {
            System.out.println("ℹ️ Desvío del flujo normal: " + descripcion);
            Paso.ejecutar(descripcion, () -> accion.accept(actor));
        }
    }
}
