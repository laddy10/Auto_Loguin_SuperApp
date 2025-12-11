package utils;

public class AdbUtils {
  public static void ejecutarAdbTap(int x, int y) {
    try {
      String comando = "adb shell input tap " + x + " " + y;
      Process process = Runtime.getRuntime().exec(comando);
      process.waitFor(); // Espera a que el comando termine
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
