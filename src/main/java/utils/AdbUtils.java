package utils;

import java.util.logging.Logger;

public class AdbUtils {

  private static final Logger LOGGER = Logger.getLogger(AdbUtils.class.getName());

  // Paquete del SIM Toolkit que muestra la publicidad del operador.
  private static final String STK_PACKAGE = "com.android.stk";

  // Serial del dispositivo por defecto. Se puede sobrescribir con -Dadb.serial=<serial>.
  private static final String DEFAULT_SERIAL = "A6FS014308000017";

  public static void ejecutarAdbTap(int x, int y) {
    runAdb("shell input tap " + x + " " + y);
  }

  /**
   * Desactiva el SIM Toolkit para que NO muestre los popups de publicidad
   * ("PUBLICIDAD DE REDBUS...") durante la automatización. Best-effort: si no se
   * puede (no hay adb, dispositivo no lo permite, etc.) se ignora.
   */
  public static void desactivarPublicidadSistema() {
    runAdb("shell pm disable-user --user 0 " + STK_PACKAGE);
    LOGGER.info("SIM Toolkit (" + STK_PACKAGE + ") desactivado para evitar publicidad.");
  }

  /** Reactiva el SIM Toolkit al terminar (restaura el estado del dispositivo). */
  public static void reactivarPublicidadSistema() {
    runAdb("shell pm enable " + STK_PACKAGE);
    LOGGER.info("SIM Toolkit (" + STK_PACKAGE + ") reactivado.");
  }

  /**
   * Ejecuta un comando adb apuntando al dispositivo con '-s'. El serial se toma de
   * la propiedad del sistema 'adb.serial' (o 'appium.udid') y, si no está definida,
   * usa {@link #DEFAULT_SERIAL}.
   */
  private static void runAdb(String args) {
    try {
      String serial = System.getProperty("adb.serial", System.getProperty("appium.udid", DEFAULT_SERIAL));
      String comando = (serial != null && !serial.trim().isEmpty())
              ? "adb -s " + serial.trim() + " " + args
              : "adb " + args;
      Process process = Runtime.getRuntime().exec(comando);
      process.waitFor();
    } catch (Exception e) {
      LOGGER.warning("No se pudo ejecutar adb (" + args + "): " + e.getMessage());
    }
  }
}
