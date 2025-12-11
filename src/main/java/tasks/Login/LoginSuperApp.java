package tasks.Login;

import interactions.Click.ClickElementByText;
import interactions.Click.ClickTextoQueContengaX;
import interactions.validations.ValidarTexto;
import interactions.validations.ValidateInformationText;
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

    public enum Metodo {CORREO, DOCUMENTO, PIN}

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
        }

        // (3) Housekeeping post-login y validación de home
        postLoginHousekeeping(actor);

        actor.attemptsTo(
                ValidarTexto.validarTexto("Tus servicios favoritos"));

        EvidenciaUtils.registrarCaptura("Login exitoso");

        actor.attemptsTo(
                Click.on(BTN_MENU_HAMBURGUESA)
        );

        actor.should(seeThat(
                ValidateInformationText.validateInformationText(LBL_ENCABEZADO_USUARIO),
                equalTo(user.getNombreUsuario())));

        EvidenciaUtils.registrarCaptura("Login exitoso con método: " + metodo);

        // (4) Cerrar sesión al finalizar
        cerrarSesion(actor);
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

    private <T extends Actor> void cerrarSesion(T actor) {

        // Click en "Cerrar sesión"
        actor.attemptsTo(
                ClickElementByText.clickElementByText(CERRAR_SESION)
        );

        // Confirmación "Sí, cerrar"
        actor.attemptsTo(
                ClickElementByText.clickElementByText(SI_CERRAR),
                WaitForResponse.withAnyText(INICIAR_SESION),
                WaitUntil.the(LBL_INICIAR_SESION, WebElementStateMatchers.isVisible()).forNoMoreThan(20).seconds()
        );

    }

    private <T extends Actor> void loginPorCorreo(T actor) {
        EvidenciaUtils.registrarCaptura("Pantalla Home - Preparar login por CORREO");
        abrirLoginDesdeHome(actor);
        bypassWelcomeBackIfPresent(actor);

        EvidenciaUtils.registrarCaptura("Pantalla: Login - Seleccionar 'Otros métodos de ingreso'");
        actor.attemptsTo(
                WaitUntil.the(BTN_OTROS_METODOS_INGRESO, WebElementStateMatchers.isVisible()).forNoMoreThan(10).seconds(),
                Click.on(BTN_OTROS_METODOS_INGRESO)
        );

        EvidenciaUtils.registrarCaptura("Pantalla: Bottom Sheet - Selección 'Correo electrónico'");
        actor.attemptsTo(
                ClickElementByText.clickElementByText(CORREO_ELECTRONICO)
        );

        EvidenciaUtils.registrarCaptura("Pantalla: Ingresar CORREO");
        actor.attemptsTo(
                WaitUntil.the(TXT_USERNAME, WebElementStateMatchers.isEnabled()).forNoMoreThan(10).seconds(),
                Click.on(TXT_USERNAME),
                Enter.theValue(user.getEmail()).into(TXT_USERNAME)
        );
        EvidenciaUtils.registrarCaptura("Correo digitado: " + (user.getEmail()));

        EvidenciaUtils.registrarCaptura("Acción: Continuar (correo)");
        actor.attemptsTo(ClickElementByText.clickElementByText(CONTINUAR));

        EvidenciaUtils.registrarCaptura("Pantalla: Ingresar CONTRASEÑA");
        actor.attemptsTo(
                WaitUntil.the(TXT_PASSWORD, WebElementStateMatchers.isEnabled()).forNoMoreThan(10).seconds(),
                Enter.theValue(user.getPassword()).into(TXT_PASSWORD)
        );
        EvidenciaUtils.registrarCaptura("Contraseña digitada: ******** (oculta)");

        EvidenciaUtils.registrarCaptura("Acción: Continuar (contraseña)");
        actor.attemptsTo(
                ClickElementByText.clickElementByText(CONTINUAR),
                WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, WebElementStateMatchers.isNotPresent()).forNoMoreThan(30).seconds()
        );

    }


    private <T extends Actor> void loginPorDocumento(T actor) {
        EvidenciaUtils.registrarCaptura("Pantalla Home - Preparar login por DOCUMENTO");
        abrirLoginDesdeHome(actor);
        bypassWelcomeBackIfPresent(actor);

        EvidenciaUtils.registrarCaptura("Pantalla: Ingresar DOCUMENTO");
        actor.attemptsTo(
                WaitUntil.the(TXT_USERNAME, WebElementStateMatchers.isEnabled()).forNoMoreThan(10).seconds(),
                Click.on(TXT_USERNAME),
                Enter.theValue(user.getCedula()).into(TXT_USERNAME)
        );
        EvidenciaUtils.registrarCaptura("Documento digitado: " + (user.getCedula()));

        EvidenciaUtils.registrarCaptura("Acción: Continuar (documento)");
        actor.attemptsTo(ClickElementByText.clickElementByText(CONTINUAR));

        EvidenciaUtils.registrarCaptura("Pantalla: Ingresar CONTRASEÑA");
        actor.attemptsTo(
                WaitUntil.the(TXT_PASSWORD, WebElementStateMatchers.isEnabled()).forNoMoreThan(10).seconds(),
                Enter.theValue(user.getPassword()).into(TXT_PASSWORD)
        );
        EvidenciaUtils.registrarCaptura("Contraseña digitada: ******** (oculta)");

        EvidenciaUtils.registrarCaptura("Acción: Continuar (contraseña)");
        actor.attemptsTo(
                ClickElementByText.clickElementByText(CONTINUAR),
                WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, WebElementStateMatchers.isNotPresent()).forNoMoreThan(30).seconds()
        );

    }


    private <T extends Actor> void loginPorPin(T actor) {
        EvidenciaUtils.registrarCaptura("Pantalla Home - Preparar login por PIN");
        abrirLoginDesdeHome(actor);

        //Manejo pantalla inicio sesión de nuevo
        bypassWelcomeBackIfPresent(actor);

        actor.attemptsTo(
                WaitUntil.the(BTN_OTROS_METODOS_INGRESO, WebElementStateMatchers.isVisible()).forNoMoreThan(10).seconds(),
                Click.on(BTN_OTROS_METODOS_INGRESO)
        );

        EvidenciaUtils.registrarCaptura("Pantalla: Bottom Sheet - Selección 'PIN'");

        actor.attemptsTo(
                ClickElementByText.clickElementByText(PIN),
                WaitUntil.the(TXT_PASSWORD, WebElementStateMatchers.isEnabled()).forNoMoreThan(10).seconds(),
                Enter.theValue(user.getNumero()).into(TXT_NUMERO)
        );

        EvidenciaUtils.registrarCaptura("Pantalla: Ingresar PIN/NÚMERO");

        actor.attemptsTo(
                ClickElementByText.clickElementByText(CONTINUAR),
                WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, WebElementStateMatchers.isNotPresent()).forNoMoreThan(30).seconds()
        );

    }

    private <T extends Actor> void bypassWelcomeBackIfPresent(T actor) {
        // ¿Está la pantalla intermedia?
        if (Presence.of(LBL_WELCOME_BACK).viewedBy(actor).resolveAll().isEmpty()) return;

        EvidenciaUtils.registrarCaptura("Intermedio 'Nos alegra tenerte de vuelta' detectado");

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
        } else {
            // Seguridad: algunos builds requieren scroll
            actor.attemptsTo(Scroll.to(LBL_INICIAR_SESION).andAlignToTop(), Click.on(LBL_INICIAR_SESION));
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
