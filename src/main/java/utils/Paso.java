package utils;

import net.thucydides.core.model.TestResult;
import net.thucydides.core.model.TestStep;
import net.thucydides.core.steps.ExecutedStepDescription;
import net.thucydides.core.steps.StepEventBus;
import net.thucydides.core.steps.StepFailure;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Pasos del reporte, en español y con lenguaje de negocio.
 *
 * Cada llamada a {@link #ejecutar} abre un paso en el reporte de Serenity con el título
 * indicado, corre el cuerpo y lo cierra con su resultado. Los pasos se pueden anidar.
 * Como las acciones de utils.Pantalla no generan pasos, el reporte queda con lo que se
 * hizo ("Ingresar la contraseña") y no con la mecánica ("actor enters '...' into ...").
 *
 * Cada paso lleva una captura del celular: la que el propio paso tome a mitad de camino
 * con {@link #capturar()} (p. ej. el formulario lleno antes de pulsar "Continuar") o, si
 * no tomó ninguna, una al terminar. Estas capturas son solo para el reporte: la carpeta
 * Capturas/ (Word y WhatsApp) solo recibe las evidencias de EvidenciaUtils.
 *
 * Si el cuerpo falla, el paso queda en rojo con el mensaje del error y una captura de
 * cómo estaba la pantalla en ese momento, y el error se propaga. La captura y el
 * mensaje se ponen solo en el paso más interno que falló; los pasos que lo contienen
 * salen en rojo por tener un hijo fallido, sin repetir la captura.
 */
public final class Paso {

    /** Errores ya reportados en un paso interno (para no repetir captura ni mensaje). */
    private static final Set<Throwable> REPORTADOS = Collections.newSetFromMap(new WeakHashMap<>());

    private Paso() {
    }

    public static void ejecutar(String titulo, Runnable cuerpo) {
        StepEventBus bus = StepEventBus.getEventBus();
        ExecutedStepDescription descripcion = ExecutedStepDescription.withTitle(titulo);
        bus.stepStarted(descripcion);
        try {
            cuerpo.run();
        } catch (Throwable t) {
            if (REPORTADOS.add(t)) {
                ReporteSerenity.adjuntarCapturaDelDispositivo("Fallo - " + titulo);
                bus.stepFailed(new StepFailure(descripcion, t));
            } else {
                // Falló un paso interno (ya tiene el mensaje y la captura): este se cierra
                // y se marca en rojo sin repetirlos.
                Optional<TestStep> estePaso = ReporteSerenity.pasoAbierto();
                bus.stepFinished();
                estePaso.ifPresent(Paso::marcarFallido);
            }
            throw propagable(t);
        }
        if (!tieneCaptura()) {
            ReporteSerenity.adjuntarCapturaDelDispositivo(titulo);
        }
        bus.stepFinished();
    }

    /**
     * Adjunta YA una captura del celular al paso en curso. Para los pasos cuyo momento
     * importante no es el final (el formulario lleno antes de enviarlo, el aviso que se
     * va a cerrar). Con eso el paso ya no toma la captura de cierre.
     */
    public static void capturar() {
        ReporteSerenity.pasoAbierto().ifPresent(p ->
                ReporteSerenity.adjuntarCapturaDelDispositivo(p.getDescription()));
    }

    private static boolean tieneCaptura() {
        return ReporteSerenity.pasoAbierto()
                .map(p -> p.getScreenshots() != null && !p.getScreenshots().isEmpty())
                .orElse(true);
    }

    /**
     * Marca un paso YA CERRADO como fallido. Serenity guarda en el JSON el resultado
     * propio de cada paso (no el de sus hijos), así que un paso padre cerrado como
     * exitoso saldría en verde aunque un hijo fallara.
     */
    public static void marcarFallido(TestStep paso) {
        try {
            paso.setResult(TestResult.FAILURE);
        } catch (RuntimeException e) {
            System.out.println("⚠️ [Reporte] No se pudo marcar el paso como fallido: " + e);
        }
    }

    /**
     * Registra un paso que no hace nada en el dispositivo pero aporta al reporte
     * (p. ej. "Tabla de resultados") y adjunta la imagen indicada.
     */
    public static void conImagen(String titulo, java.awt.image.BufferedImage imagen) {
        ejecutar(titulo, () -> ReporteSerenity.adjuntarImagen(titulo, imagen, true));
    }

    /**
     * Paso que falla con el mensaje indicado, sin captura del dispositivo (para
     * conclusiones como "fallaron 1 de 4 inicios de sesión", donde la pantalla
     * actual no dice nada).
     */
    public static void fallarSinCaptura(String titulo, String mensaje) {
        AssertionError error = new AssertionError(mensaje);
        REPORTADOS.add(error);
        StepEventBus bus = StepEventBus.getEventBus();
        ExecutedStepDescription descripcion = ExecutedStepDescription.withTitle(titulo);
        bus.stepStarted(descripcion);
        bus.stepFailed(new StepFailure(descripcion, error));
        throw error;
    }

    private static RuntimeException propagable(Throwable t) {
        if (t instanceof RuntimeException) {
            return (RuntimeException) t;
        }
        if (t instanceof Error) {
            throw (Error) t;
        }
        RuntimeException envuelto = new RuntimeException(t);
        REPORTADOS.add(envuelto);
        return envuelto;
    }
}
