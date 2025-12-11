package utils;

public class EstadoPrueba {
  public static boolean fallo = false;
  public static String pasoFallido = "";
  public static long inicio = 0L;
  public static long fin = 0L;

  public static void reset() {
    fallo = false;
    pasoFallido = null;
    inicio = System.currentTimeMillis();
    fin = 0;
  }
}
