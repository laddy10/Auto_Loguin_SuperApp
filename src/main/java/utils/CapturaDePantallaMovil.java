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

  public static String tomarCapturaPantalla(String nombreCaptura) {
    String rutaDestino = "";
    try {
      // Obtener fecha y hora actual en formato seguro para nombres de archivo
      //   String formattedDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm_ss"));
      String nombreNormalizado = nombreCaptura
              .toLowerCase()
              .replaceAll("[^a-z0-9]", "_");  // Reemplaza todo lo que no sea letra o número

      String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
      String nombreArchivo = nombreNormalizado + "_" + timestamp + ".png";
      rutaDestino = CAPTURAS_DIR + nombreArchivo;
      //  rutaDestino = CAPTURAS_DIR + nombreArchivo;

      // Verificar si la carpeta Capturas/ existe, si no, crearla
      File carpetaCapturas = new File(CAPTURAS_DIR);
      if (!carpetaCapturas.exists()) {
        carpetaCapturas.mkdirs();
      }

      // Obtener el WebDriver actual y tomar la captura
      TakesScreenshot screenshotTaker = (TakesScreenshot) SerenityWebdriverManager.inThisTestThread().getCurrentDriver();
      File captura = screenshotTaker.getScreenshotAs(OutputType.FILE);
      File destinoTemporal = new File(CAPTURAS_DIR + "temp.png");

      // Guardar la captura temporalmente
      FileUtils.copyFile(captura, destinoTemporal);

      // Cargar la imagen correctamente
      BufferedImage imagen = ImageIO.read(destinoTemporal);
      if (imagen == null) {
        LOGGER.severe("No se pudo cargar la imagen, ImageIO.read() devolvió null.");
        return "";
      }

      // Crear una nueva imagen con el mismo tamaño
      BufferedImage imagenConBorde = new BufferedImage(imagen.getWidth(), imagen.getHeight(), BufferedImage.TYPE_INT_RGB);
      Graphics2D g2d = imagenConBorde.createGraphics();
      g2d.drawImage(imagen, 0, 0, null);

      // Dibujar el borde rojo
      g2d.setColor(Color.WHITE);
      g2d.setStroke(new BasicStroke(5)); // Grosor del borde
      g2d.drawRect(2, 2, imagen.getWidth() - 4, imagen.getHeight() - 4);
      g2d.dispose();

      // Guardar la imagen con el borde
      File destinoFinal = new File(rutaDestino);
      ImageIO.write(imagenConBorde, "png", destinoFinal);

      // Eliminar el archivo temporal
      destinoTemporal.delete();

      LOGGER.info("Captura de pantalla guardada con borde rojo: " + rutaDestino);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Error al tomar o guardar la captura de pantalla: " + nombreCaptura, e);
    }
    return rutaDestino;
  }
}