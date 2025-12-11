package stepDefinitions;

import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import interactions.wait.WaitFor;
import interactions.wait.WaitForResponse;

import models.User;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.actors.OnlineCast;
import net.serenitybdd.screenplay.waits.WaitUntil;
import tasks.Login.*;
import tasks.Ollama.ValidateLocatorsWithOllama;
import utils.EvidenciaUtils;
import utils.TestDataProvider;
import utils.WordAppium;

import static net.serenitybdd.screenplay.actors.OnStage.theActorCalled;
import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isNotPresent;
import static userinterfaces.LoginPage.*;
import static utils.Constants.*;

public class LoginDefinitions {

    private final User user = TestDataProvider.getRealUser();


    @Before
    public void initScenario(Scenario scenario) {
        OnStage.setTheStage(new OnlineCast());
        WordAppium.inicializarPlantillaReporte();
        EvidenciaUtils.reiniciarContador(); // Reinicia el conteo de pasos para este escenario
    }


    @Given("EL USUARIO ABRE LA SUPER APP")
    public void abrirSuperApp() {
        theActorCalled("actor")
                .attemptsTo(
                        WaitUntil.the(LOADING_SPLASH, isNotPresent()),
                        WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, isNotPresent()).forNoMoreThan(40).seconds(),
                        WaitFor.aTime(2000)
                );
    }

    @When("^REALIZA EL INGRESO$")
    public void ingresoSuperApp() {
        theActorInTheSpotlight().attemptsTo(
                IngresoSuperApp.ingresoSuperApp()
        );
    }

    @Then("^VERIFICA VERSION DE LA SUPER APP$")
    public void verificaVersion() {
        theActorInTheSpotlight().attemptsTo(
                VersionSuperApp.validarVersion()
        );
    }

    @When("^REALIZA EL INGRESO CON CEDULA$")
    public void ingresoConCedula() {
        theActorInTheSpotlight().attemptsTo(
                LoginConCedula.conCedula()
        );
    }


    @When("^REALIZA EL INGRESO CON CORREO$")
    public void ingresoCorreo() {
        theActorInTheSpotlight().attemptsTo(
                LoginSuperApp.con(LoginSuperApp.Metodo.CORREO)
        );
    }

    @When("^REALIZA EL INGRESO CON DOCUMENTO$")
    public void ingresoDocumento() {
        theActorInTheSpotlight().attemptsTo(
                LoginSuperApp.con(LoginSuperApp.Metodo.DOCUMENTO)
        );
    }

    @When("^REALIZA EL INGRESO CON PIN$")
    public void ingresoPIN() {
        theActorInTheSpotlight().attemptsTo(
                LoginSuperApp.con(LoginSuperApp.Metodo.PIN)
        );
    }



   /* @When("verifico los siguientes localizadores o textos {string} contra el page source {string}")
    public void verificoLosLocalizadoresContraElPageSource(String locatorsOrTexts, String xmlFileName) {
        // Leer maxLocatorsToSend desde serenity.properties si prefieres (por ejemplo 100)
        int maxLocatorsToSend = 100; // ajustable o configurable

        OnStage.theActorInTheSpotlight().attemptsTo(
                ValidateLocatorsWithOllama.using(xmlFileName, locatorsOrTexts, maxLocatorsToSend)
        );
    }*/
}