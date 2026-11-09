package utils;

import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.model.TestStep;
import net.thucydides.core.screenshots.ScreenshotAndHtmlSource;
import net.thucydides.core.steps.BaseStepListener;
import net.thucydides.core.steps.StepEventBus;
import net.thucydides.core.webdriver.SerenityWebdriverManager;
import net.thucydides.core.webdriver.WebDriverFacade;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Field;
import java.text.Normalizer;
import java.util.Iterator;
import java.util.Optional;
import java.util.Stack;

/**
 * Adjunta imágenes al paso ABIERTO del reporte de Serenity.
 *
 * El proyecto trabaja con serenity.take.screenshots=DISABLED: las capturas de la
 * carpeta Capturas/ (Word y WhatsApp) son las mismas que se adjuntan aquí, así que el
 * dispositivo se fotografía una sola vez por evidencia. La imagen se guarda reducida
 * (JPG de 720 px de ancho) en la carpeta de salida de Serenity y se enlaza al paso;
 * Smart Tester la muestra en /reporte/:id igual que una captura nativa de Serenity.
 *
 * Nada de esto puede tumbar la prueba: cualquier problema se registra en consola y se
 * sigue sin la imagen.
 */
public final class ReporteSerenity {

    private static final int ANCHO_MAXIMO = 720;
    private static final float CALIDAD_JPG = 0.82f;
    private static int secuencia = 0;

    private ReporteSerenity() {
    }

    /**
     * @param nitida true para imágenes generadas con texto pequeño (tabla de resultados):
     *               se guardan en PNG y sin reducir.
     */
    public static void adjuntarImagen(String nombre, BufferedImage imagen, boolean nitida) {
        if (imagen == null) {
            return;
        }
        try {
            Optional<TestStep> paso = pasoAbierto();
            if (!paso.isPresent()) {
                System.out.println("⚠️ [Reporte] No hay un paso abierto para adjuntar «" + nombre + "».");
                return;
            }
            File carpeta = carpetaDeSalida();
            carpeta.mkdirs();
            String base = String.format("evidencia_%d_%02d_%s", System.currentTimeMillis(),
                    ++secuencia, slug(nombre));
            File destino;
            if (nitida) {
                destino = new File(carpeta, base + ".png");
                ImageIO.write(imagen, "png", destino);
            } else {
                destino = new File(carpeta, base + ".jpg");
                escribirJpg(reducir(imagen), destino);
            }
            paso.get().addScreenshot(new ScreenshotAndHtmlSource(destino));
        } catch (Throwable t) {
            System.out.println("⚠️ [Reporte] No se pudo adjuntar la imagen «" + nombre + "»: " + t);
        }
    }

    /** Toma una captura del dispositivo (solo si ya hay sesión de Appium) y la adjunta. */
    public static void adjuntarCapturaDelDispositivo(String nombre) {
        long inicio = System.currentTimeMillis();
        BufferedImage captura = capturarDispositivo();
        if (captura != null) {
            adjuntarImagen(nombre, captura, false);
            System.out.println("📷 [Reporte] " + nombre + " ("
                    + (System.currentTimeMillis() - inicio) + " ms)");
        }
    }

    /**
     * Captura de la pantalla del dispositivo, o null si no hay driver abierto o falla.
     * Nunca crea un driver nuevo (si la sesión de Appium no arrancó, no hay nada que ver).
     */
    public static BufferedImage capturarDispositivo() {
        try {
            WebDriver driver = SerenityWebdriverManager.inThisTestThread().getCurrentDriver();
            if (driver instanceof WebDriverFacade) {
                WebDriverFacade facade = (WebDriverFacade) driver;
                if (!facade.isInstantiated()) {
                    return null;
                }
                driver = facade.getProxiedDriver();
            }
            if (!(driver instanceof TakesScreenshot)) {
                return null;
            }
            byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            return bytes == null ? null : ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (Throwable t) {
            System.out.println("⚠️ [Reporte] No se pudo capturar la pantalla del dispositivo: " + t);
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * El paso en curso es el tope de la pila de pasos del BaseStepListener: es donde el
     * mismo Serenity pone sus capturas. Esa pila es privada (Serenity 2.x), de ahí la
     * reflexión; si cambiara, se usa el paso actual del TestOutcome.
     */
    @SuppressWarnings("unchecked")
    public static Optional<TestStep> pasoAbierto() {
        BaseStepListener listener = StepEventBus.getEventBus().getBaseStepListener();
        try {
            Field campo = BaseStepListener.class.getDeclaredField("currentStepStack");
            campo.setAccessible(true);
            Stack<TestStep> pila = (Stack<TestStep>) campo.get(listener);
            if (pila != null && !pila.isEmpty()) {
                return Optional.of(pila.peek());
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // se intenta por la vía pública
        }
        return listener.latestTestOutcome().flatMap(TestOutcome::currentStep);
    }

    private static File carpetaDeSalida() {
        try {
            Field campo = BaseStepListener.class.getDeclaredField("outputDirectory");
            campo.setAccessible(true);
            Object valor = campo.get(StepEventBus.getEventBus().getBaseStepListener());
            if (valor instanceof File) {
                return (File) valor;
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // valor por defecto
        }
        return new File(System.getProperty("serenity.outputDirectory", "target/site/serenity"));
    }

    private static BufferedImage reducir(BufferedImage original) {
        if (original.getWidth() <= ANCHO_MAXIMO) {
            return aRgb(original);
        }
        int alto = (int) Math.round(original.getHeight() * (ANCHO_MAXIMO / (double) original.getWidth()));
        BufferedImage reducida = new BufferedImage(ANCHO_MAXIMO, alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = reducida.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(original, 0, 0, ANCHO_MAXIMO, alto, null);
        g.dispose();
        return reducida;
    }

    /** JPG no admite canal alfa: se pasa a RGB. */
    private static BufferedImage aRgb(BufferedImage imagen) {
        if (imagen.getType() == BufferedImage.TYPE_INT_RGB) {
            return imagen;
        }
        BufferedImage rgb = new BufferedImage(imagen.getWidth(), imagen.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.drawImage(imagen, 0, 0, null);
        g.dispose();
        return rgb;
    }

    private static void escribirJpg(BufferedImage imagen, File destino) throws java.io.IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            ImageIO.write(imagen, "jpg", destino);
            return;
        }
        ImageWriter writer = writers.next();
        ImageWriteParam parametros = writer.getDefaultWriteParam();
        parametros.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        parametros.setCompressionQuality(CALIDAD_JPG);
        try (ImageOutputStream salida = ImageIO.createImageOutputStream(destino)) {
            writer.setOutput(salida);
            writer.write(null, new IIOImage(imagen, null, null), parametros);
        } finally {
            writer.dispose();
        }
    }

    private static String slug(String texto) {
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String s = sinTildes.toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
        return s.length() > 40 ? s.substring(0, 40) : s;
    }
}
