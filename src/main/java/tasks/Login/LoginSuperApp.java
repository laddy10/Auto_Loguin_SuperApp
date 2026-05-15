package tasks.Login;

import interactions.Click.ClickElementByText;
import interactions.Click.ClickTextoQueContengaX;
import interactions.comunes.Atras;
import interactions.validations.ValidarTexto;
import interactions.validations.ValidateInformationText;
import interactions.wait.WaitFor;
import interactions.wait.WaitForResponse;
import models.User;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Enter;
import net.serenitybdd.screenplay.actions.Scroll;
import net.serenitybdd.screenplay.matchers.WebElementStateMatchers;
import net.serenitybdd.screenplay.questions.Presence;
import net.serenitybdd.screenplay.waits.WaitUntil;
import utils.EvidenciaUtils;
import utils.TestDataProvider;

import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static net.serenitybdd.screenplay.Tasks.instrumented;
import static org.hamcrest.core.IsEqual.equalTo;
import static userinterfaces.LoginPage.*;
import static utils.Constants.*;

public class LoginSuperApp implements Task {

    public enum Metodo {CORREO, DOCUMENTO, PIN, TODOS}

    private final LoginSuperApp.Metodo metodo;
    private final User user = TestDataProvider.getRealUser();

    public LoginSuperApp(LoginSuperApp.Metodo metodo) {
        this.metodo = metodo;
    }

    @Override
    public <T extends Actor> void performAs(T actor) {

        if (!Presence.of(LBL_SESION_CERRADA_POR_SEGURIDAD).viewedBy(actor).resolveAll().isEmpty()) {
            actor.attemptsTo(
                    ClickTextoQueContengaX.elTextoContiene(INICIAR_SESION)
            );
        }
        if (!Presence.of(BTN_CANCELAR_INICIO).viewedBy(actor).resolveAll().isEmpty()) {
            actor.attemptsTo(
                    ClickTextoQueContengaX.elTextoContiene(CANCELAR)
            );
        }

        // (1) Si hay sesión abierta → cerrar
        asegurarSesionCerrada(actor);


        // (2) Login según método solicitado
        switch (metodo) {
            case CORREO:
                loginPorCorreo(actor);
                break;
            case DOCUMENTO:
                loginPorDocumento(actor);
                break;
            case PIN:
                loginPorPin(actor);
                break;
            case TODOS:
                loginCompleto(actor);
                break;
        }

        // (3) Housekeeping post-login y validación de home
        // postLoginHousekeeping(actor);
    }


    private <T extends Actor> void asegurarSesionCerrada(T actor) {
        // Si aparece Hola, Gerencia en home → ir a Perfil y cerrar sesión

        if (!Presence.of(BTN_MENU_HAMBURGUESA).viewedBy(actor).resolveAll().isEmpty()) {
            actor.attemptsTo(
                    Click.on(BTN_MENU_HAMBURGUESA)
            );

            cerrarSesion(actor);
        }

    }

    private <T extends Actor> void validarLoginYCerrarSesion(T actor) {
        actor.attemptsTo(
                ValidarTexto.validarTexto("Tus servicios favoritos"),
                Click.on(BTN_MENU_HAMBURGUESA)
        );
        actor.should(seeThat(
                ValidateInformationText.validateInformationText(LBL_ENCABEZADO_USUARIO),
                equalTo(user.getNombreUsuario())));
        cerrarSesion(actor);
    }

    private <T extends Actor> void cerrarSesion(T actor) {

        // Click en "Cerrar sesión"
        actor.attemptsTo(
                ClickElementByText.clickElementByText(CERRAR_SESION)
        );

        // Confirmación "Sí, cerrar"
        actor.attemptsTo(
                ClickElementByText.clickElementByText(SI_CERRAR),
                WaitForResponse.withAnyText(INICIAR_SESION),
                WaitUntil.the(LBL_INICIAR_SESION, WebElementStateMatchers.isVisible()).forNoMoreThan(15).seconds()
        );

    }

    private <T extends Actor> void loginPorCorreo(T actor) {
        //EvidenciaUtils.registrarCaptura("Preparar login por CORREO");
        abrirLoginDesdeHome(actor);
        bypassWelcomeBackIfPresent(actor);

        //EvidenciaUtils.registrarCaptura("Pantalla Login Seleccionar Otros metodos de ingreso");
        actor.attemptsTo(
                WaitUntil.the(BTN_OTROS_METODOS_INGRESO, WebElementStateMatchers.isVisible()).forNoMoreThan(15).seconds(),
                Click.on(BTN_OTROS_METODOS_INGRESO)
        );

        //EvidenciaUtils.registrarCaptura("Seleccion Correo electronico");
        actor.attemptsTo(
                ClickElementByText.clickElementByText(CORREO_ELECTRONICO)
        );

        actor.attemptsTo(
                WaitUntil.the(TXT_USERNAME, WebElementStateMatchers.isEnabled()).forNoMoreThan(15).seconds(),
                Click.on(TXT_USERNAME),
                Enter.theValue(user.getEmail()).into(TXT_USERNAME),
                Atras.irAtras(),
                WaitFor.aTime(500)
        );

        EvidenciaUtils.registrarCaptura("Ingresar con Correo");
        actor.attemptsTo(ClickElementByText.clickElementByText(CONTINUAR));

        actor.attemptsTo(
                WaitUntil.the(TXT_PASSWORD, WebElementStateMatchers.isEnabled()).forNoMoreThan(15).seconds(),
                Enter.theValue(user.getPassword()).into(TXT_PASSWORD)
        );

        actor.attemptsTo(
                ClickElementByText.clickElementByText(CONTINUAR),
                WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, WebElementStateMatchers.isNotPresent()).forNoMoreThan(35).seconds()
        );
        if (!Presence.of(BTN_ACEPTAR).viewedBy(actor).resolveAll().isEmpty()) {
            actor.attemptsTo(
                    ClickElementByText.clickElementByText(ACEPTAR_2),
                    ClickElementByText.clickElementByText(CONTINUAR),
                    WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, WebElementStateMatchers.isNotPresent()).forNoMoreThan(35).seconds()
            );
        }

    }


    private <T extends Actor> void loginPorDocumento(T actor) {
        //EvidenciaUtils.registrarCaptura("Preparar login por DOCUMENTO");
        abrirLoginDesdeHome(actor);
        bypassWelcomeBackIfPresent(actor);

        actor.attemptsTo(
                WaitUntil.the(TXT_USERNAME, WebElementStateMatchers.isEnabled()).forNoMoreThan(15).seconds(),
                Click.on(TXT_USERNAME),
                Enter.theValue(user.getCedula()).into(TXT_USERNAME),
                Atras.irAtras(),
                WaitFor.aTime(500)
        );

        EvidenciaUtils.registrarCaptura("Ingresar con documento");
        actor.attemptsTo(ClickElementByText.clickElementByText(CONTINUAR));

        actor.attemptsTo(
                WaitUntil.the(TXT_PASSWORD, WebElementStateMatchers.isEnabled()).forNoMoreThan(15).seconds(),
                Enter.theValue(user.getPassword()).into(TXT_PASSWORD)
        );

        actor.attemptsTo(
                ClickElementByText.clickElementByText(CONTINUAR),
                WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, WebElementStateMatchers.isNotPresent()).forNoMoreThan(35).seconds()
        );

        if (!Presence.of(BTN_ACEPTAR).viewedBy(actor).resolveAll().isEmpty()) {
            actor.attemptsTo(
                    ClickElementByText.clickElementByText(ACEPTAR_2),
                    ClickElementByText.clickElementByText(CONTINUAR),
                    WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, WebElementStateMatchers.isNotPresent()).forNoMoreThan(35).seconds()
            );
        }

    }

    private <T extends Actor> void olvideLaContrasenaDocumento(T actor) {
        //EvidenciaUtils.registrarCaptura("Preparar login por DOCUMENTO");
        abrirLoginDesdeHome(actor);
        bypassWelcomeBackIfPresent(actor);

        actor.attemptsTo(
                WaitUntil.the(TXT_USERNAME, WebElementStateMatchers.isEnabled()).forNoMoreThan(15).seconds(),
                Click.on(TXT_USERNAME),
                Enter.theValue(user.getCedula()).into(TXT_USERNAME),
                Atras.irAtras()
        );

        actor.attemptsTo(
                ClickElementByText.clickElementByText(CONTINUAR),
                Click.on(BTN_OLVIDE_CONTRASENA),
                WaitFor.aTime(2000),
                WaitUntil.the(TXT_PASSWORD, WebElementStateMatchers.isPresent()).forNoMoreThan(15).seconds());

        actor.attemptsTo(
                WaitUntil.the(TXT_USERNAME, WebElementStateMatchers.isEnabled()).forNoMoreThan(15).seconds(),
                Click.on(TXT_USERNAME),
                Enter.theValue(user.getCedula()).into(TXT_USERNAME),
                Atras.irAtras(),
                ClickElementByText.clickElementByText(CONTINUAR),
                WaitUntil.the(LBL_CREA_NUEVA_CONTRASENA, WebElementStateMatchers.isPresent()).forNoMoreThan(15).seconds());

        EvidenciaUtils.registrarCaptura("Olvide la contrasena");
        actor.attemptsTo(
                Click.on(BTN_FLECHA_ATRAS),
                Click.on(BTN_VOLVER_PAGINA_PRINCIPAL)
        );
    }


    private <T extends Actor> void loginPorPin(T actor) {
        //EvidenciaUtils.registrarCaptura("Preparar login por PIN");
        abrirLoginDesdeHome(actor);

        //Manejo pantalla inicio sesión de nuevo
        bypassWelcomeBackIfPresent(actor);

        actor.attemptsTo(
                WaitUntil.the(BTN_OTROS_METODOS_INGRESO, WebElementStateMatchers.isVisible()).forNoMoreThan(15).seconds(),
                Click.on(BTN_OTROS_METODOS_INGRESO)
        );

        //EvidenciaUtils.registrarCaptura("Seleccion PIN");

        actor.attemptsTo(
                ClickElementByText.clickElementByText(PIN),
                WaitUntil.the(TXT_PASSWORD, WebElementStateMatchers.isEnabled()).forNoMoreThan(15).seconds(),
                Enter.theValue(user.getNumero()).into(TXT_NUMERO)
        );

        try {
            actor.attemptsTo(
                    ClickElementByText.clickElementByText(CONTINUAR),

                    WaitUntil.the(LBL_INGRESA_PIN, WebElementStateMatchers.isPresent()).forNoMoreThan(10).seconds()
                    //WaitFor.aTime(1000)
                    //WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, WebElementStateMatchers.isNotPresent()).forNoMoreThan(5).seconds()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        EvidenciaUtils.registrarCaptura("Ingresar PIN");

        actor.attemptsTo(
                WaitFor.aTime(1000),
                WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, WebElementStateMatchers.isNotPresent()).forNoMoreThan(35).seconds()
        );
        if (!Presence.of(BTN_ACEPTAR).viewedBy(actor).resolveAll().isEmpty()) {
            actor.attemptsTo(
                    ClickElementByText.clickElementByText(ACEPTAR_2),
                    ClickElementByText.clickElementByText(CONTINUAR),
                    WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, WebElementStateMatchers.isNotPresent()).forNoMoreThan(35).seconds()
            );
        }

        if (!Presence.of(LBL_SESION_ABIERTA).viewedBy(actor).resolveAll().isEmpty()) {
            actor.attemptsTo(
                    ClickElementByText.clickElementByText(CONTINUAR),
                    WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, WebElementStateMatchers.isNotPresent()).forNoMoreThan(35).seconds()
            );
        }

    }

    private <T extends Actor> void loginCompleto(T actor) {
        olvideLaContrasenaDocumento(actor);
        loginPorPin(actor);
        validarLoginYCerrarSesion(actor);
        loginPorCorreo(actor);
        validarLoginYCerrarSesion(actor);
        loginPorDocumento(actor);

        actor.attemptsTo(
                ValidarTexto.validarTexto("Tus servicios favoritos")
        );
        EvidenciaUtils.registrarCaptura("Login exitoso");
        validarLoginYCerrarSesion(actor);
    }

    private <T extends Actor> void bypassWelcomeBackIfPresent(T actor) {
        // ¿Está la pantalla intermedia?
        //if (Presence.of(LBL_WELCOME_BACK).viewedBy(actor).resolveAll().isEmpty()) return;


        // 1) Preferir 'Ingresar con otra cuenta' si está visible & habilitado
        if (!Presence.of(LNK_INGRESAR_OTRA_CUENTA).viewedBy(actor).resolveAll().isEmpty()) {
            if (LNK_INGRESAR_OTRA_CUENTA.resolveFor(actor).isClickable() || LNK_INGRESAR_OTRA_CUENTA.resolveFor(actor).isEnabled()) {
                actor.attemptsTo(Click.on(LNK_INGRESAR_OTRA_CUENTA));
                return;
            }
        }

        // 2) Si no hay link, intentar 'Continuar' SOLO si está habilitado
        if (!Presence.of(BTN_CONTINUAR_WELCOME).viewedBy(actor).resolveAll().isEmpty()) {
            // Espera breve a que se habilite; si no, no intentes el click
            actor.attemptsTo(WaitUntil.the(BTN_CONTINUAR_WELCOME, WebElementStateMatchers.isVisible())
                    .forNoMoreThan(5).seconds());
            boolean enabled = false;
            try {
                enabled = BTN_CONTINUAR_WELCOME.resolveFor(actor).isEnabled();
            } catch (Exception ignored) {
            }

            if (enabled) {
                actor.attemptsTo(Click.on(BTN_CONTINUAR_WELCOME));
                // tras continuar, suele aparecer el link; si aparece, clic
                if (!Presence.of(LNK_INGRESAR_OTRA_CUENTA).viewedBy(actor).resolveAll().isEmpty()) {
                    actor.attemptsTo(Click.on(LNK_INGRESAR_OTRA_CUENTA));
                    return;
                }
            }
        }
    }

    private <T extends Actor> void abrirLoginDesdeHome(T actor) {
        if (!Presence.of(LBL_INICIAR_SESION).viewedBy(actor).resolveAll().isEmpty()) {
            actor.attemptsTo(Click.on(LBL_INICIAR_SESION));
        //} else {
            // Seguridad: algunos builds requieren scroll
            //actor.attemptsTo(Scroll.to(LBL_INICIAR_SESION).andAlignToTop(), Click.on(LBL_INICIAR_SESION));
        }
    }

    private <T extends Actor> void postLoginHousekeeping(T actor) {
        if (!Presence.of(LBL_INGRESO_BIOMETRICO).viewedBy(actor).resolveAll().isEmpty()) {
            actor.attemptsTo(ClickElementByText.clickElementByText(EN_OTRO_MOMENTO));
        }
        if (!Presence.of(LBL_SESION_ABIERTA).viewedBy(actor).resolveAll().isEmpty()) {
            actor.attemptsTo(ClickElementByText.clickElementByText(CONTINUAR));
        }
        if (!Presence.of(LBL_TERMINOS_Y_CONDICIONES).viewedBy(actor).resolveAll().isEmpty()) {
            actor.attemptsTo(Click.on(CHECK_TERMINOS_Y_CONDICIONES),
                    ClickElementByText.clickElementByText(CONTINUAR));
        }
        if (!Presence.of(LBL_INGRESO_BIOMETRICO).viewedBy(actor).resolveAll().isEmpty()) {
            actor.attemptsTo(
                    ClickElementByText.clickElementByText("En otro momento"));
        }
    }


    public static Performable con(LoginSuperApp.Metodo metodo) {
        return instrumented(LoginSuperApp.class, metodo);
    }
}
