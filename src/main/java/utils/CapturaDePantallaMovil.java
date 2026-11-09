package utils;

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

  /**
   * Captura de pantalla con nombre de archivo FIJO (sin timestamp): los reintentos
   * reescriben la misma imagen en lugar de acumular capturas. Así queda una sola
   * evidencia por paso.
   */
  public static BufferedImage tomarCapturaPantallaFija(String nombreCaptura) {
    String nombreNormalizado = nombreCaptura.toLowerCase().replaceAll("[^a-z0-9]", "_");
    String rutaDestino = CAPTURAS_DIR + nombreNormalizado + ".png";
    return escribirCapturaConBorde(rutaDestino, nombreCaptura);
  }

  /**
   * Toma la captura del dispositivo, le pone el borde blanco y la guarda en rutaDestino.
   * Devuelve la imagen (para adjuntarla también al reporte sin otra captura) o null si
   * no se pudo tomar. Nunca lanza.
   */
  private static BufferedImage escribirCapturaConBorde(String rutaDestino, String nombreCaptura) {
    try {
      File carpetaCapturas = new File(CAPTURAS_DIR);
      if (!carpetaCapturas.exists()) {
        carpetaCapturas.mkdirs();
      }

      BufferedImage imagen = ReporteSerenity.capturarDispositivo();
      if (imagen == null) {
        LOGGER.severe("No se pudo tomar la captura del dispositivo: " + nombreCaptura);
        return null;
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

      LOGGER.info("Captura de pantalla guardada con borde: " + rutaDestino);
      return imagenConBorde;
    } catch (IOException | RuntimeException e) {
      LOGGER.log(Level.SEVERE, "Error al tomar o guardar la captura de pantalla: " + nombreCaptura, e);
      return null;
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
