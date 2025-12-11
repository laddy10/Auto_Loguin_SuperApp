package utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

/**
 * Ingresa los datos obtenidos del archivo de Excel al archivo .feature del cual se esta llamando.
 *
 * @author js.siabato@mensajerosurbanos.com
 * @since 27/04/2021
 */
public class DataToFeature {

  /**
   * Ingresa los datos obtenidos de un excel al archivo .feature del cual se esta llamando, hace que
   * se genere la tabla en el escenario.
   *
   * @param featureFile Nombre del archivo .feature el cual se modificará, debe tener la ruta del
   *     archivo y la hoja a ser usada.
   * @return
   * @throws InvalidFormatException Manejo de error por formato invalido
   * @throws IOException Manejo de error para el proceso de entrada y salida de datos
   * @since 27/04/2021
   */
  private static List<String> setExcelDataToFeature(File featureFile)
      throws InvalidFormatException, IOException {
    SeleniumFunctions functions = new SeleniumFunctions();
    List<String> fileData = new ArrayList<String>();
    try (BufferedReader buffReader =
        new BufferedReader(
            new InputStreamReader(
                new BufferedInputStream(new FileInputStream(featureFile)), "UTF-8"))) {
      String data;
      List<Map<String, String>> excelData;
      boolean foundHashTag = false;
      boolean featureData = false;
      boolean esUnRango = false;
      boolean esMultiple = false;
      boolean esRangoDefinido = false;
      while ((data = buffReader.readLine()) != null) {
        String[] dataVector;
        String[] dataVectorRango = null;
        String sheetName = null;
        String excelFilePath = null;
        String excelDataRange = null;
        int filaSeleccionada = 0;
        int pos = 0;

        if (data.trim().contains("##@externalData")) {
          dataVector = data.trim().split("@");
          String route = dataVector[2];
          String dat[] = route.split(".xlsx");
          String file = dat[0] + functions.getScenarioData("Environment") + ".xlsx";
          excelFilePath = file;
          sheetName = dataVector[3];
          if (dataVector.length == 4) {
            esUnRango = true;
          }
          if (dataVector.length == 5) {
            if (dataVector[4].contains("-")) {
              dataVectorRango = dataVector[4].trim().split("-");
              esRangoDefinido = true;
              filaSeleccionada = Integer.parseInt(dataVectorRango[pos]) - 1;
            } else {
              if (dataVector[4].contains(",")) {
                dataVectorRango = dataVector[4].trim().split(",");
                esUnRango = true;
                esMultiple = true;
                filaSeleccionada = Integer.parseInt(dataVectorRango[pos]) - 1;
              } else {
                filaSeleccionada = Integer.parseInt(dataVector[4]) - 1;
              }
            }
          }
          foundHashTag = true;
          fileData.add(data);
        }
        if (foundHashTag) {
          excelData = new LectorExcel().getData(excelFilePath, sheetName);
          for (int rowNumber = filaSeleccionada; rowNumber < excelData.size() - 1; rowNumber++) {
            String cellData = "";
            for (Entry<String, String> mapData : excelData.get(rowNumber).entrySet()) {
              if (dataVectorRango == null) {
                if (rowNumber == filaSeleccionada - 1 && !esUnRango) {
                  cellData = cellData + "   |" + mapData.getValue();
                } else {
                  cellData = cellData + "   |" + mapData.getValue();
                }
              } else {
                if (esRangoDefinido) {
                  if (rowNumber < Integer.parseInt(dataVectorRango[1])) {
                    cellData = cellData + "   |" + mapData.getValue();
                  }
                } else {
                  if (rowNumber + 1 == Integer.parseInt(dataVectorRango[pos]) && esUnRango) {
                    cellData = cellData + "   |" + mapData.getValue();
                  }
                }
              }
            }
            fileData.add(cellData + "|");
            if (!esUnRango) {
              if (!esRangoDefinido) rowNumber = excelData.size();
            }
            if (esMultiple) {
              if (pos + 1 < dataVectorRango.length) {
                filaSeleccionada = Integer.parseInt(dataVectorRango[pos + 1]) - 1;
                rowNumber = filaSeleccionada - 1;
                pos++;
              } else {
                rowNumber = excelData.size() - 1;
              }
            }
            if (esRangoDefinido) {
              if (rowNumber + 1 == Integer.parseInt(dataVectorRango[1])) {
                rowNumber = excelData.size() - 1;
                pos++;
              } else {
                pos++;
              }
            }
          }
          foundHashTag = false;
          featureData = true;
          continue;
        }
        if (data.startsWith("|") || data.endsWith("|")) {
          if (!featureData) {
            fileData.add(data);
          }
          continue;
        } else {
          featureData = false;
        }
        fileData.add(data);
      }
    }
    return fileData;
  }

  /**
   * Lista de todos los .feature con sus respectivos archivos de excel que se usarán en la prueba.
   *
   * @param folder Carpeta donde estarán los archivos .feature
   * @return
   * @since 27/04/2021
   */
  private static List<File> listOfFeatureFiles(File folder) {
    List<File> featureFiles = new ArrayList<File>();
    if (folder.getName().endsWith(".feature")) {
      featureFiles.add(folder);
    } else {
      for (File fileEntry : folder.listFiles()) {
        if (fileEntry.isDirectory()) {
          featureFiles.addAll(listOfFeatureFiles(fileEntry));
        } else {
          if (fileEntry.isFile() && fileEntry.getName().endsWith(".feature")) {
            featureFiles.add(fileEntry);
          }
        }
      }
    }
    return featureFiles;
  }

  /**
   * Hace una lista con todos los .feature dependiendo de la ruta asignada
   *
   * @param featuresDirectoryPath Ruta donde se encuentra los .feature que tendrán las tablas
   * @throws IOException Manejo de error para el proceso de entrada y salida de datos
   * @throws InvalidFormatException Manejo de error por formato invalido
   * @since 27/04/2021
   */
  public static void overrideFeatureFiles(String featuresDirectoryPath)
      throws IOException, InvalidFormatException {
    List<File> listOfFeatureFiles = listOfFeatureFiles(new File(featuresDirectoryPath));
    for (File featureFile : listOfFeatureFiles) {
      List<String> featureWithExcelData = setExcelDataToFeature(featureFile);
      try (BufferedWriter writer =
          new BufferedWriter(
              new OutputStreamWriter(new FileOutputStream(featureFile), "UTF-8")); ) {
        for (String string : featureWithExcelData) {
          writer.write(string);
          writer.write("\n");
        }
      }
    }
  }

  /**
   * Realiza un BackUp del archivo .feature
   *
   * @since 27/04/2021
   */
  public static void backUpFeaturesFile() {
    try {
      FileUtils.copyDirectory(
          new File("./src/test/resources/features/"),
          new File("./src/test/resources/backup/features/"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Restaura el archivo .feature a como estaba antes de la modificación.
   *
   * @since 27/04/2021
   */
  public static void restoreBackUpFeatures() {
    try {
      FileUtils.copyDirectory(
          new File("./src/test/resources/backup/features/"),
          new File("./src/test/resources/features/"));
      FileUtils.deleteDirectory(new File("./src/test/resources/backup/features/"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
