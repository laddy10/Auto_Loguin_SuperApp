package tasks.Login;

import interactions.Click.ClickElementByText;
import interactions.Click.ClickTextoQueContengaX;
import interactions.comunes.Atras;
import interactions.validations.ValidarTextoQueContengaX;
import interactions.validations.ValidateInformationText;
import interactions.wait.WaitElement;
import interactions.wait.WaitFor;
import models.User;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Enter;
import net.serenitybdd.screenplay.questions.Presence;
import net.serenitybdd.screenplay.targets.Target;
import net.serenitybdd.screenplay.waits.WaitUntil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import utils.AndroidObject;
import utils.EvidenciaUtils;
import utils.TestDataProvider;

import java.util.List;

import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static net.serenitybdd.screenplay.Tasks.instrumented;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isNotPresent;
import static org.hamcrest.core.IsEqual.equalTo;
import static userinterfaces.LoginPage.*;
import static utils.Constants.*;

public class IngresoSuperApp implements Task {

    private final User user = TestDataProvider.getRealUser();
    private static final String paso = "Login exitoso a la Super App";

    @Override
    public <T extends Actor> void performAs(T actor) {

        if (isVisible(actor, LBL_ENCABEZADO_USUARIO)) {
            String encabezado = LBL_ENCABEZADO_USUARIO.resolveFor(actor).getText();

            if (encabezado.contains("Hola, Gerencia")) {
                // Ya logueado, solo capturas
                EvidenciaUtils.registrarCaptura("Usuario ya tiene sesión iniciada");
                return; // Se corta aquí
            }
            if (encabezado.contains("¡Hola!")) {
                // Usuario sin login, se continúa con flujo
                EvidenciaUtils.registrarCaptura("Usuario sin sesión, se inicia login");
            }

        }

        // 2. Elegir la ruta según prioridad usando if / else if
        if (isVisible(actor, LBL_SESION_CERRADA_POR_SEGURIDAD)) {
            EvidenciaUtils.registrarCaptura("Ruta: sesión cerrada por seguridad detectada");
            clickAceptarSesion(actor);
            loginViaIniciar(actor);

        } else if (isVisible(actor, LBL_NOS_ALEGRA_TENERTE_DE_VUELTA)) {
            EvidenciaUtils.registrarCaptura("Ruta: mensaje de bienvenida detectado");
            loginViaIniciar(actor);

        } else if (isVisible(actor, LBL_INICIAR_SESION)) {
            EvidenciaUtils.registrarCaptura("Ruta: botón iniciar sesión visible");
            actor.attemptsTo(Click.on(LBL_INICIAR_SESION));
            loginViaIniciar(actor);

        } else if (isVisible(actor, BTN_OTROS_METODOS_INGRESO) && isValidEmail(user.getEmail())) {
            EvidenciaUtils.registrarCaptura("Ruta: otros métodos + email válido");
            loginConEmail(actor);

        } else if (isVisible(actor, TXT_USERNAME) && isValidCedula(user.getCedula())) {
            EvidenciaUtils.registrarCaptura("Ruta: campo usuario visible + cédula válida");
            loginConCedula(actor);

        } else {
            EvidenciaUtils.registrarCaptura("Ruta: fallback - login desde cero completo");
            loginDesdeCeroCompleto(actor);
        }

        // 3. Validación final de que el login fue exitoso
        actor.should(seeThat(ValidateInformationText.validateInformationText(LBL_ENCABEZADO_USUARIO),
                equalTo(user.getNombreUsuario())));
        EvidenciaUtils.registrarCaptura(paso);
    }

    /**
     * Ruta común para iniciar sesión + validar login.
     */
    private <T extends Actor> void loginViaIniciar(T actor) {
        iniciarSesion(actor);
        validarLogin(actor);
    }

    private boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty();
    }

    private boolean isValidCedula(String cedula) {
        return cedula != null && !cedula.trim().isEmpty();
    }

    private <T extends Actor> void loginConEmail(T actor) {
        actor.attemptsTo(
                ClickElementByText.clickElementByText(OTROS_METODOS_DE_INGRESO),
                ClickElementByText.clickElementByText(CORREO_ELECTRONICO),
                WaitElement.isEnable(TXT_USERNAME),
                Enter.theValue(user.getEmail()).into(TXT_USERNAME),
                ClickElementByText.clickElementByText(CONTINUAR),
                Enter.theValue(user.getPassword()).into(TXT_PASSWORD),
                ClickElementByText.clickElementByText(CONTINUAR),
                WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, isNotPresent()).forNoMoreThan(30).seconds()
        );
        validarLogin(actor);
    }

    private <T extends Actor> void loginConCedula(T actor) {
        actor.attemptsTo(
                Enter.theValue(user.getCedula()).into(TXT_USERNAME),
                ClickElementByText.clickElementByText(CONTINUAR),
                Enter.theValue(user.getPassword()).into(TXT_PASSWORD),
                ClickElementByText.clickElementByText(CONTINUAR),
                WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, isNotPresent()).forNoMoreThan(30).seconds()
        );
        validarLogin(actor);
    }

    private <T extends Actor> void loginDesdeCeroCompleto(T actor) {
        aceptarPermisosIniciales(actor);
        loginDesdeCero(actor);
        validarLogin(actor);
    }

    private <T extends Actor> void SesiónCerradaPorSeguridad(T actor) {
        actor.attemptsTo(
                ClickElementByText.clickElementByText(CONTINUAR),
                Enter.theValue(user.getPassword()).into(TXT_PASSWORD),
                ClickElementByText.clickElementByText(CONTINUAR),
                WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, isNotPresent()).forNoMoreThan(30).seconds()
        );
    }

    private <T extends Actor> void iniciarSesion(T actor) {
        if (isVisible(actor, LBL_INICIAR_SESION)){
            actor.attemptsTo(ClickTextoQueContengaX.elTextoContiene(INICIAR_SESION));
        }
        actor.attemptsTo(
                ClickElementByText.clickElementByText(CONTINUAR),
                Enter.theValue(user.getPassword()).into(TXT_PASSWORD),
                ClickElementByText.clickElementByText(CONTINUAR),
                WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, isNotPresent()).forNoMoreThan(30).seconds()
        );
    }

    private <T extends Actor> void clickAceptarSesion(T actor) {
        if (isVisible(actor, BTN_ACEPTAR)) {
            actor.attemptsTo(ClickElementByText.clickElementByText(ACEPTAR));
        } else {
            actor.attemptsTo(ClickElementByText.clickElementByText(ACEPTAR_2));
        }
    }

    private <T extends Actor> void aceptarPermisosIniciales(T actor) {
        clickSiExiste(actor, BTN_PERMISO_UBICACION, MIENTRAS_APP_ESTA_EN_USO);
        clickSiExiste(actor, BTN_ACEPTAR_PERMISO, ACEPTAR_2);
        clickSiExiste(actor, SMS_PERMISO_LLAMADAS, NO_PERMITIR);
        clickSiExiste(actor, SMS_PERMISO_NOTIFICACIONES, NO_PERMITIR);
        clickSiExiste(actor, BTN_OMITIR, OMITIR);
        clickSiExisteCheckboxYContinuar(actor, LBL_BIENVENIDA, CHECK_TC, CONTINUAR);
        clickSiExiste(actor, TXT_AUTORIZACION_VELOCIDAD, ACEPTAR_2);
    }

    private <T extends Actor> void loginDesdeCero(T actor) {
        actor.attemptsTo(ClickElementByText.clickElementByText(INICIAR_SESION));

        ValidarTextoQueContengaX.elTextoContiene(VERSION);

        if (isValidEmail(user.getEmail())) {
            actor.attemptsTo(
                    ClickElementByText.clickElementByText(OTROS_METODOS_DE_INGRESO),
                    ClickElementByText.clickElementByText(CORREO_ELECTRONICO),
                    WaitElement.isEnable(TXT_USERNAME),
                    Enter.theValue(user.getEmail()).into(TXT_USERNAME)
            );
        } else {
            actor.attemptsTo(
                    WaitElement.isEnable(TXT_USERNAME),
                    Enter.theValue(user.getCedula()).into(TXT_USERNAME)
            );
        }

        actor.attemptsTo(
                ClickElementByText.clickElementByText(CONTINUAR),
                Enter.theValue(user.getPassword()).into(TXT_PASSWORD),
                ClickElementByText.clickElementByText(CONTINUAR),
                WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, isNotPresent()).forNoMoreThan(30).seconds()
        );
    }

    private <T extends Actor> void validarLogin(T actor) {
        // Si ya está logueado, no continuar
        if (isUserAlreadyLoggedIn(actor)) {
            return;
        }

        maybeAceptarTerminos(actor);
        maybeManejarSesionAbierta(actor);
        maybeManejarBiometrico(actor);
        maybePermisoNotificaciones(actor);
        maybeAutorizarVelocidad(actor);

        // Validación final del login exitoso
        actor.should(seeThat(ValidateInformationText.validateInformationText(LBL_ENCABEZADO_USUARIO),
                equalTo(user.getNombreUsuario())));
    }

    private <T extends Actor> void maybeAceptarTerminos(T actor) {
        if (isVisibleFast(actor, LBL_TERMINOS_Y_CONDICIONES)) {
            actor.attemptsTo(
                    Click.on(CHECK_TERMINOS_Y_CONDICIONES),
                    ClickElementByText.clickElementByText(CONTINUAR)
            );
        }
    }

    private <T extends Actor> void maybeManejarSesionAbierta(T actor) {
        if (isVisibleFast(actor, LBL_SESION_ABIERTA)) {
            actor.attemptsTo(
                    ClickElementByText.clickElementByText(CONTINUAR),
                    WaitFor.aTime(6000)
            );
        }
    }

    private <T extends Actor> void maybeManejarBiometrico(T actor) {
        if (isVisibleFast(actor, LBL_INGRESO_BIOMETRICO)) {
            actor.attemptsTo(ClickElementByText.clickElementByText("En otro momento"));
        }
    }

    private <T extends Actor> void maybePermisoNotificaciones(T actor) {
        clickSiExisteFast(actor, SMS_PERMISO_NOTIFICACIONES, NO_PERMITIR);
    }

    private <T extends Actor> void maybeAutorizarVelocidad(T actor) {
        if (isVisibleFast(actor, TXT_AUTORIZACION_VELOCIDAD)) {
            actor.attemptsTo(WaitFor.aTime(1000),
                    ClickElementByText.clickElementByText(ACEPTAR));
        }
        if (isVisibleFast(actor, TXT_AUTORIZACION_VELOCIDAD_2)) {
            actor.attemptsTo(
                    WaitFor.aTime(1000),
                    ClickElementByText.clickElementByText(ACEPTAR),
                    Atras.irAtras()
            );
        }
    }

    private <T extends Actor> boolean isUserAlreadyLoggedIn(T actor) {
        try {
            List<WebElement> userElements = AndroidObject.androidDriver(actor)
                    .findElements(By.xpath("//android.widget.TextView[contains(@text, '" + user.getNombreUsuario() + "')]"));
            return !userElements.isEmpty() && userElements.get(0).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    private <T extends Actor> boolean isVisibleFast(T actor, Target element) {
        try {
            return !Presence.of(element).viewedBy(actor).resolveAll().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private <T extends Actor> boolean isVisible(T actor, Target element) {
        try {
            return !Presence.of(element).viewedBy(actor).resolveAll().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private <T extends Actor> void clickSiExisteFast(T actor, Target elemento, String texto) {
        if (isVisibleFast(actor, elemento)) {
            actor.attemptsTo(ClickElementByText.clickElementByText(texto));
        }
    }

    private <T extends Actor> void clickSiExiste(T actor, Target elemento, String texto) {
        if (isVisible(actor, elemento)) {
            actor.attemptsTo(ClickElementByText.clickElementByText(texto));
        } else {
            actor.attemptsTo(WaitFor.aTime(1000));
        }
    }

    private <T extends Actor> void clickSiExisteCheckboxYContinuar(T actor, Target condicion, Target checkbox, String botonTexto) {
        if (isVisible(actor, condicion)) {
            actor.attemptsTo(
                    Click.on(checkbox),
                    ClickElementByText.clickElementByText(botonTexto)
            );
        }
    }

    public static Performable ingresoSuperApp() {
        return instrumented(IngresoSuperApp.class);
    }
}
