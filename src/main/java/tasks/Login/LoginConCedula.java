package tasks.Login;

import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static net.serenitybdd.screenplay.Tasks.instrumented;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isNotPresent;
import static org.hamcrest.core.IsEqual.equalTo;
import static userinterfaces.LoginPage.*;
import static utils.Constants.*;

import interactions.Click.ClickElementByText;
import interactions.comunes.Atras;
import interactions.validations.ValidarTextoQueContengaX;
import interactions.validations.ValidateInformationText;
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
import utils.EvidenciaUtils;
import utils.TestDataProvider;

public class LoginConCedula implements Task {

  private final User user = TestDataProvider.getRealUser();
  private static final String paso = "Login exitoso con cédula";

  @Override
  public <T extends Actor> void performAs(T actor) {

    if (isVisible(actor, LBL_ENCABEZADO_USUARIO)) {
      String textoVisible =
              ValidateInformationText.validateInformationText(LBL_ENCABEZADO_USUARIO).answeredBy(actor);

      if ("¡Hola!".equals(textoVisible)) {
        // Texto es "Hola" => NO hay sesión iniciada, sigue con el flujo normal (no retornes)
      } else {
        // Texto diferente a "Hola" => sesión iniciada, valida y termina el flujo
        actor.should(
                seeThat(
                        ValidateInformationText.validateInformationText(LBL_ENCABEZADO_USUARIO),
                        equalTo(user.getNombreUsuario())));
        EvidenciaUtils.registrarCaptura(paso);
        return;
      }
    }

    if (isVisible(actor, LBL_SESION_CERRADA_POR_SEGURIDAD)) {
      clickAceptarSesion(actor);
      iniciarSesion(actor);
      validarLogin(actor);
      EvidenciaUtils.registrarCaptura(paso);
      return;
    }

    if (isVisible(actor, LBL_NOS_ALEGRA_TENERTE_DE_VUELTA)) {
      iniciarSesion(actor);
      validarLogin(actor);
      EvidenciaUtils.registrarCaptura(paso);
      return;
    }

    if (isVisible(actor, LBL_INICIAR_SESION)) {
      actor.attemptsTo(Click.on(LBL_INICIAR_SESION));
      iniciarSesion(actor);
      validarLogin(actor);
      EvidenciaUtils.registrarCaptura(paso);
      return;
    }

    if (isVisible(actor, TXT_USERNAME)) {
      actor.attemptsTo(
              Enter.theValue(user.getCedula()).into(TXT_USERNAME),
              ClickElementByText.clickElementByText(CONTINUAR),
              Enter.theValue(user.getPassword()).into(TXT_PASSWORD),
              ClickElementByText.clickElementByText(CONTINUAR),
              WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, isNotPresent()).forNoMoreThan(30).seconds());
    }

    aceptarPermisosIniciales(actor);
    loginDesdeCero(actor);
    validarLogin(actor);
    EvidenciaUtils.registrarCaptura(paso);
  }

  private <T extends Actor> void iniciarSesion(T actor) {
    actor.attemptsTo(
            ClickElementByText.clickElementByText(CONTINUAR),
            Enter.theValue(user.getPassword()).into(TXT_PASSWORD),
            ClickElementByText.clickElementByText(CONTINUAR),
            WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, isNotPresent()).forNoMoreThan(30).seconds());
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
    clickSiExiste(actor, SMS_PERMISO_NOTIFICACIONES2, NO_PERMITIR);
    clickSiExiste(actor, BTN_OMITIR, OMITIR);
    clickSiExisteCheckboxYContinuar(actor, LBL_BIENVENIDA, CHECK_TC, CONTINUAR);
    clickSiExiste(actor, SMS_PERMISO_NOTIFICACIONES2, NO_PERMITIR);
    clickSiExiste(actor, TXT_AUTORIZACION_VELOCIDAD, ACEPTAR_2);
  }

  private <T extends Actor> void loginDesdeCero(T actor) {
    actor.attemptsTo(
            ClickElementByText.clickElementByText(INICIAR_SESION),
            ValidarTextoQueContengaX.elTextoContiene(VERSION),
            Enter.theValue(user.getCedula()).into(TXT_USERNAME),
            ClickElementByText.clickElementByText(CONTINUAR),
            Enter.theValue(user.getPassword()).into(TXT_PASSWORD),
            ClickElementByText.clickElementByText(CONTINUAR),
            WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, isNotPresent()).forNoMoreThan(30).seconds());
  }

  private <T extends Actor> void validarLogin(T actor) {
    if (isVisible(actor, LBL_TERMINOS_Y_CONDICIONES)) {
      actor.attemptsTo(
              Click.on(CHECK_TERMINOS_Y_CONDICIONES), ClickElementByText.clickElementByText(CONTINUAR));
    } else {
      actor.attemptsTo(WaitFor.aTime(1000));
    }

    if (isVisible(actor, LBL_SESION_ABIERTA)) {
      actor.attemptsTo(ClickElementByText.clickElementByText(CONTINUAR), WaitFor.aTime(6000));
    }

    if (isVisible(actor, LBL_INGRESO_BIOMETRICO)) {
      actor.attemptsTo(ClickElementByText.clickElementByText("En otro momento"));
    }

    clickSiExiste(actor, SMS_PERMISO_NOTIFICACIONES2, NO_PERMITIR);

    if (isVisible(actor, TXT_AUTORIZACION_VELOCIDAD)) {
      actor.attemptsTo(WaitFor.aTime(1000), ClickElementByText.clickElementByText(ACEPTAR));
    }

    if (isVisible(actor, TXT_AUTORIZACION_VELOCIDAD_2)) {
      actor.attemptsTo(
              WaitFor.aTime(1000), ClickElementByText.clickElementByText(ACEPTAR), Atras.irAtras());
    }

    actor.should(
            seeThat(
                    ValidateInformationText.validateInformationText(LBL_ENCABEZADO_USUARIO),
                    equalTo(user.getNombreUsuario())));
  }

  private <T extends Actor> boolean isVisible(T actor, Target element) {
    return !Presence.of(element).viewedBy(actor).resolveAll().isEmpty();
  }

  private <T extends Actor> void clickSiExiste(T actor, Target elemento, String texto) {
    if (isVisible(actor, elemento)) {
      actor.attemptsTo(ClickElementByText.clickElementByText(texto));
    } else {
      actor.attemptsTo(WaitFor.aTime(1000));
    }
  }

  private <T extends Actor> void clickSiExisteCheckboxYContinuar(
          T actor, Target condicion, Target checkbox, String botonTexto) {
    if (isVisible(actor, condicion)) {
      actor.attemptsTo(Click.on(checkbox), ClickElementByText.clickElementByText(botonTexto));
    }
  }

  public static Performable conCedula() {
    return instrumented(LoginConCedula.class);
  }
}