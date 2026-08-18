package hooks;

import java.util.ArrayList;
import java.util.List;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import listeners.OllamaStepListener;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.actors.OnlineCast;
import net.thucydides.core.steps.StepEventBus;
import utils.AdbUtils;
import utils.EstadoPrueba;
import utils.WordAppium;

/**
 * Hooks consolidados de Cucumber
 *
 * Responsabilidades:
 * - Inicializar actores de Serenity
 * - Registrar OllamaStepListener
 * - Generar reportes Word
 * - Tracking de pasos y estado
 */
public class ReportHooks {

  private static final List<String> pasosEjecutados = new ArrayList<>();
  private static String lineaUsada = "Sin datos";
  private static String ultimoPaso = "";
  private static boolean listenerRegistrado = false;

  public static void registrarPaso(String paso) {
    pasosEjecutados.add(paso);
    ultimoPaso = paso;
  }

  public static void setLinea(String linea) {
    lineaUsada = linea;
  }

  @Before(order = 0) // ✅ Ejecutar PRIMERO
  public void initScenario(Scenario scenario) {
    System.out.println("\n══════════════════════════════════════════════════════");
    System.out.println("🚀 Iniciando escenario: " + scenario.getName());
    System.out.println("══════════════════════════════════════════════════════");

    // 🔹 Inicializar estado de prueba
    EstadoPrueba.inicio = System.currentTimeMillis();
    pasosEjecutados.clear();
    EstadoPrueba.fallo = false;
    EstadoPrueba.pasoFallido = "";

    // 🔹 Desactivar la publicidad del SIM Toolkit (com.android.stk) para que no
    //    interrumpa la automatización con popups de "PUBLICIDAD...".
    AdbUtils.desactivarPublicidadSistema();

   /* // 🔹 Registrar el listener de Ollama solo una vez
    if (!listenerRegistrado) {
      try {
        OllamaStepListener ollamaListener = new OllamaStepListener();
        StepEventBus.getEventBus().registerListener(ollamaListener);
        listenerRegistrado = true;
        System.out.println("✅ [OLLAMA] Listener registrado correctamente");
      } catch (Exception e) {
        System.err.println("❌ [OLLAMA] Error al registrar listener: " + e.getMessage());
        e.printStackTrace();
      }
    }*/

    // 🔹 Inicializar actores de Serenity
    OnStage.setTheStage(new OnlineCast());
  }

  @After(order = 1) // ✅ Ejecutar DESPUÉS de otros @After
  public void generarReporteFinal(Scenario scenario) {
    EstadoPrueba.fin = System.currentTimeMillis();

    // Detectar fallo y último paso fallido
    if (scenario.isFailed()) {
      EstadoPrueba.fallo = true;
      EstadoPrueba.pasoFallido = !pasosEjecutados.isEmpty() ? ultimoPaso : "Paso no identificado";
    }

    long duracionTotal = (EstadoPrueba.fin - EstadoPrueba.inicio) / 1000;
    long minutos = duracionTotal / 60;
    long segundos = duracionTotal % 60;
    String duracionFormato = minutos + " min " + segundos + " seg";

    String estadoFinal = scenario.isFailed() ? "FAILED" : "PASSED";
    String pasoFallido = scenario.isFailed() ? EstadoPrueba.pasoFallido : null;

    WordAppium.generarReporte(
            scenario.getName(),
            pasosEjecutados.toArray(new String[0]),
            lineaUsada,
            duracionFormato,
            pasoFallido,
            estadoFinal);

    System.out.println("══════════════════════════════════════════════════════");
    System.out.println("🏁 Escenario finalizado: " + scenario.getName());
    System.out.println("   Estado: " + (scenario.isFailed() ? "❌ FAILED" : "✅ PASSED"));
    System.out.println("   Duración: " + duracionFormato);
    System.out.println("══════════════════════════════════════════════════════\n");

    // Reactivar el SIM Toolkit para restaurar el estado del dispositivo.
    AdbUtils.reactivarPublicidadSistema();

    // Limpiar estado para el siguiente escenario
    pasosEjecutados.clear();
    EstadoPrueba.fallo = false;
    EstadoPrueba.pasoFallido = "";
  }
}