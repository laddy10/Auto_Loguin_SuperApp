package tasks.Login;

import net.thucydides.core.model.TestStep;
import net.thucydides.core.steps.StepEventBus;
import utils.EvidenciaUtils;
import utils.Paso;
import utils.ReporteSerenity;
import utils.TablaResultados;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Monitoreo de los 4 inicios de sesión (@LOGIN_COMPLETO).
 *
 * Cada inicio de sesión es un paso del escenario. Si uno falla, su paso queda en rojo
 * con el motivo y la captura, se reabre la app y se sigue con el siguiente: el
 * monitoreo siempre prueba los 4. Al final se genera la tabla de resultados y, si
 * alguno falló, el escenario termina FALLIDO con el resumen (antes terminaba exitoso
 * aunque la tabla dijera FALLO, y el reporte de Smart Tester salía en verde).
 */
public class MonitoreoLogin {

    /** Orden de las filas en la tabla (el del formato de monitoreo). */
    private static final String[] ORDEN_TABLA = {
            "Login por PIN",
            "Login Por Correo y Contraseña",
            "Login por Documento",
            "Cambio de Contraseña"
    };

    private final FlujosLogin flujos;
    private final List<TablaResultados.Fila> filas = new ArrayList<>();
    private final List<String> fallos = new ArrayList<>();
    /**
     * Pasos del escenario ("Y inicia sesión con PIN") cuyo inicio de sesión falló. Cucumber
     * los cierra como exitosos (el fallo no se propaga, para seguir con el siguiente), así
     * que se marcan en rojo una vez cerrados.
     */
    private final List<TestStep> pasosFallidos = new ArrayList<>();

    public MonitoreoLogin(FlujosLogin flujos) {
        this.flujos = flujos;
    }

    /**
     * Ejecuta un inicio de sesión sin dejar que su fallo detenga el monitoreo.
     *
     * @param etiqueta nombre de la fila en la tabla de resultados
     */
    public void ejecutar(String etiqueta, Runnable flujo) {
        marcarPasosFallidos();
        // Un fallo anterior deja a Serenity en modo "saltar pasos": se limpia para que
        // este inicio de sesión se ejecute y se reporte normalmente.
        StepEventBus.getEventBus().clearStepFailures();
        boolean exitoso = false;
        try {
            flujo.run();
            exitoso = true;
        } catch (Throwable t) {
            System.out.println("⚠️ [" + etiqueta + "] Falló: " + t);
            fallos.add(etiqueta + ": " + mensaje(t));
            ReporteSerenity.pasoAbierto().ifPresent(pasosFallidos::add);
            StepEventBus.getEventBus().clearStepFailures();
            Paso.ejecutar("Reabrir la app para seguir con el siguiente inicio de sesión", flujos::reabrirApp);
        }
        StepEventBus.getEventBus().clearStepFailures();
        String hora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        filas.add(new TablaResultados.Fila(etiqueta, exitoso, hora, "Android"));
    }

    /** Tabla de resultados (Capturas/, Word, WhatsApp y reporte) y veredicto final. */
    public void concluir() {
        marcarPasosFallidos();
        Paso.ejecutar("Generar la tabla de resultados del monitoreo", () -> {
            try {
                BufferedImage tabla = TablaResultados.crearImagen(ordenarParaTabla());
                EvidenciaUtils.registrarImagen("Tabla de resultados del Login Completo", tabla);
            } catch (Throwable t) {
                // La tabla es informativa: si no se puede dibujar, el veredicto sigue.
                System.out.println("⚠️ No se pudo generar la tabla de resultados: " + t);
            }
        });

        if (!fallos.isEmpty()) {
            Paso.fallarSinCaptura(
                    "Validar que los " + filas.size() + " inicios de sesión fueron exitosos",
                    "Falló " + fallos.size() + " de " + filas.size() + " inicios de sesión. "
                            + String.join(" | ", fallos));
        }
    }

    /** Marca en rojo los pasos de los inicios de sesión que fallaron (ya cerrados). */
    private void marcarPasosFallidos() {
        for (TestStep paso : pasosFallidos) {
            Paso.marcarFallido(paso);
        }
        pasosFallidos.clear();
    }

    private List<TablaResultados.Fila> ordenarParaTabla() {
        List<TablaResultados.Fila> ordenadas = new ArrayList<>();
        for (String etiqueta : ORDEN_TABLA) {
            for (TablaResultados.Fila f : filas) {
                if (f.metodo.equals(etiqueta)) {
                    ordenadas.add(f);
                    break;
                }
            }
        }
        for (TablaResultados.Fila f : filas) {
            if (!ordenadas.contains(f)) {
                ordenadas.add(f);
            }
        }
        return ordenadas;
    }

    private static String mensaje(Throwable t) {
        Throwable raiz = t;
        while (raiz.getCause() != null && raiz.getMessage() == null) {
            raiz = raiz.getCause();
        }
        String m = raiz.getMessage() == null ? raiz.getClass().getSimpleName() : raiz.getMessage();
        m = m.replaceAll("\\s+", " ").trim();
        return m.length() > 400 ? m.substring(0, 397) + "..." : m;
    }
}
