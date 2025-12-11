package utils;

import static userinterfaces.LoginPage.*;

import models.User;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.questions.Presence;
import net.serenitybdd.screenplay.questions.Text;

public class LoginUtils {

    public static void validarSesionActivaYContinuar(Actor actor, User user) {
        if (!Presence.of(LBL_IDENTIFICADOR_USUARIO).viewedBy(actor).resolveAll().isEmpty()) {
            String textoUsuario = Text.of(LBL_IDENTIFICADOR_USUARIO).viewedBy(actor).asString().toLowerCase();

            boolean estaLogueadoConCorreo = textoUsuario.contains("@");
            boolean estaLogueadoConCedula = textoUsuario.contains("cédula");

            boolean pruebaEsConCorreo = user.getEmail() != null && !user.getEmail().isEmpty();
            boolean pruebaEsConCedula = user.getCedula() != null && !user.getCedula().isEmpty();

            boolean sesionInvalida =
                    (estaLogueadoConCorreo && pruebaEsConCedula)
                            || (estaLogueadoConCedula && pruebaEsConCorreo);

            if (sesionInvalida) {
                // Necesitamos cambiar de cuenta
                actor.attemptsTo(Click.on(BTN_INGRESAR_OTRA_CUENTA));

            } else {
                // Podemos continuar
                actor.attemptsTo(Click.on(BTN_CONTINUAR));
            }
        }
    }
}