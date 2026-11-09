package stepDefinitions;

import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.es.Cuando;
import cucumber.api.java.es.Dado;
import cucumber.api.java.es.Entonces;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.actors.OnlineCast;
import tasks.Login.FlujosLogin;
import tasks.Login.MonitoreoLogin;
import utils.CapturaDePantallaMovil;
import utils.EvidenciaUtils;
import utils.WordAppium;

import static net.serenitybdd.screenplay.actors.OnStage.theActorCalled;

/**
 * Pasos del feature login.feature.
 *
 * Los pasos de inicio de sesión se comportan distinto según el escenario:
 *  - Tags individuales (@LOGIN_PIN_01, ...): si el inicio de sesión falla, el paso falla.
 *  - Monitoreo (@LOGIN_COMPLETO): un fallo no detiene el resto; queda en rojo en su paso,
 *    se anota en la tabla y el último paso ("todos los inicios de sesión son exitosos")
 *    falla con el resumen.
 */
public class LoginDefinitions {

    private static final String TAG_MONITOREO = "@LOGIN_COMPLETO";

    private boolean esMonitoreo;
    private FlujosLogin flujos;
    private MonitoreoLogin monitoreo;

    @Before
    public void initScenario(Scenario scenario) {
        OnStage.setTheStage(new OnlineCast());
        WordAppium.inicializarPlantillaReporte();
        EvidenciaUtils.reiniciarContador(); // Reinicia el conteo de pasos para este escenario

        esMonitoreo = scenario.getSourceTagNames().contains(TAG_MONITOREO);
        if (esMonitoreo) {
            // Carpeta limpia: al final solo deben quedar las 5 evidencias + la tabla.
            CapturaDePantallaMovil.limpiarCapturas();
        }
    }

    @Dado("^que el usuario abre la app Mi Claro$")
    public void abrirSuperApp() {
        Actor usuario = theActorCalled("Usuario");
        flujos = new FlujosLogin(usuario);
        monitoreo = new MonitoreoLogin(flujos);
        flujos.esperarQueCargueLaApp();
    }

    // ── Inicios de sesión (individuales o monitoreo) ─────────────────────────

    @Cuando("^valida el cambio de contraseña$")
    public void cambioDeContrasena() {
        iniciarSesion("Cambio de Contraseña", () -> flujos.cambioDeContrasena());
    }

    @Cuando("^inicia sesión con PIN$")
    public void ingresoPin() {
        String evidencia = esMonitoreo ? "Login exitoso" : "Login exitoso PIN";
        iniciarSesion("Login por PIN", () -> flujos.loginPorPin(evidencia));
    }

    @Cuando("^inicia sesión con correo y contraseña$")
    public void ingresoCorreo() {
        String evidencia = esMonitoreo ? "Login exitoso" : "Login exitoso Correo";
        iniciarSesion("Login Por Correo y Contraseña", () -> flujos.loginPorCorreo(evidencia));
    }

    @Cuando("^inicia sesión con documento$")
    public void ingresoDocumento() {
        String evidencia = esMonitoreo ? "Login exitoso" : "Login exitoso Documento";
        iniciarSesion("Login por Documento", () -> flujos.loginPorDocumento(evidencia));
    }

    @Entonces("^todos los inicios de sesión son exitosos$")
    public void concluirMonitoreo() {
        monitoreo.concluir();
    }

    private void iniciarSesion(String etiqueta, Runnable flujo) {
        if (esMonitoreo) {
            monitoreo.ejecutar(etiqueta, flujo);
        } else {
            flujo.run();
        }
    }
}
