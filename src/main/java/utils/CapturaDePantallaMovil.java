package utils;

import net.thucydides.core.webdriver.SerenityWebdriverManager;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CapturaDePantallaMovil {

  private static final Logger LOGGER = Logger.getLogger(CapturaDePantallaMovil.class.getName());
  private static final String CAPTURAS_DIR = "Capturas/";

  /** Captura de pantalla con nombre único (incluye timestamp). */
  public static String tomarCapturaPantalla(String nombreCaptura) {
    String nombreNormalizado = nombreCaptura.toLowerCase().replaceAll("[^a-z0-9]", "_");
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    String rutaDestino = CAPTURAS_DIR + nombreNormalizado + "_" + timestamp + ".png";
    escribirCapturaConBorde(rutaDestino, nombreCaptura);
    return rutaDestino;
  }

  /**
   * Igual que {@link #tomarCapturaPantalla} pero con nombre de archivo FIJO (sin
   * timestamp): los reintentos reescriben la misma imagen en lugar de acumular
   * capturas. Útil para dejar solo una evidencia por paso.
   */
  public static String tomarCapturaPantallaFija(String nombreCaptura) {
    String nombreNormalizado = nombreCaptura.toLowerCase().replaceAll("[^a-z0-9]", "_");
    String rutaDestino = CAPTURAS_DIR + nombreNormalizado + ".png";
    escribirCapturaConBorde(rutaDestino, nombreCaptura);
    return rutaDestino;
  }

  private static void escribirCapturaConBorde(String rutaDestino, String nombreCaptura) {
    try {
      File carpetaCapturas = new File(CAPTURAS_DIR);
      if (!carpetaCapturas.exists()) {
        carpetaCapturas.mkdirs();
      }

      // Obtener el WebDriver actual y tomar la captura
      TakesScreenshot screenshotTaker =
              (TakesScreenshot) SerenityWebdriverManager.inThisTestThread().getCurrentDriver();
      File captura = screenshotTaker.getScreenshotAs(OutputType.FILE);
      File destinoTemporal = new File(CAPTURAS_DIR + "temp.png");
      FileUtils.copyFile(captura, destinoTemporal);

      BufferedImage imagen = ImageIO.read(destinoTemporal);
      if (imagen == null) {
        LOGGER.severe("No se pudo cargar la imagen, ImageIO.read() devolvió null.");
        return;
      }

      // Imagen con borde blanco (igual que antes)
      BufferedImage imagenConBorde =
              new BufferedImage(imagen.getWidth(), imagen.getHeight(), BufferedImage.TYPE_INT_RGB);
      Graphics2D g2d = imagenConBorde.createGraphics();
      g2d.drawImage(imagen, 0, 0, null);
      g2d.setColor(Color.WHITE);
      g2d.setStroke(new BasicStroke(5));
      g2d.drawRect(2, 2, imagen.getWidth() - 4, imagen.getHeight() - 4);
      g2d.dispose();

      ImageIO.write(imagenConBorde, "png", new File(rutaDestino));
      destinoTemporal.delete();

      LOGGER.info("Captura de pantalla guardada con borde: " + rutaDestino);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Error al tomar o guardar la captura de pantalla: " + nombreCaptura, e);
    }
  }

  /**
   * Guarda una imagen ya generada (ej. la tabla de resultados) en la carpeta
   * Capturas/ usando la misma convención de nombres que las capturas de pantalla,
   * para que el informe Word la asocie automáticamente con su paso.
   */
  public static String guardarImagen(String nombreCaptura, BufferedImage imagen) {
    String rutaDestino = "";
    try {
      String nombreNormalizado = nombreCaptura.toLowerCase().replaceAll("[^a-z0-9]", "_");
      String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
      rutaDestino = CAPTURAS_DIR + nombreNormalizado + "_" + timestamp + ".png";

      File carpetaCapturas = new File(CAPTURAS_DIR);
      if (!carpetaCapturas.exists()) {
        carpetaCapturas.mkdirs();
      }

      ImageIO.write(imagen, "png", new File(rutaDestino));
      LOGGER.info("Imagen personalizada guardada: " + rutaDestino);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Error al guardar la imagen personalizada: " + nombreCaptura, e);
    }
    return rutaDestino;
  }

  /** Borra los PNG de la carpeta Capturas/ para empezar una corrida limpia. */
  public static void limpiarCapturas() {
    File carpeta = new File(CAPTURAS_DIR);
    File[] archivos = carpeta.listFiles();
    if (archivos != null) {
      for (File f : archivos) {
        if (f.isFile() && f.getName().toLowerCase().endsWith(".png")) {
          f.delete();
        }
      }
    }
  }
}
