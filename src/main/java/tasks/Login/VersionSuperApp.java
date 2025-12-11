package tasks.Login;

import static net.serenitybdd.screenplay.Tasks.instrumented;
import static utils.Constants.*;

import interactions.Click.ClickTextoQueContengaX;
import interactions.validations.ValidarTexto;
import interactions.validations.ValidarTextoQueContengaX;
import models.User;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import utils.EvidenciaUtils;
import utils.TestDataProvider;

public class VersionSuperApp implements Task {
  private final User user = TestDataProvider.getRealUser();
  private static final String paso = "Información Inicial\n" + "a. Validar versión APP.\n";

  @Override
  public <T extends Actor> void performAs(T actor) {
    actor.attemptsTo(
        ClickTextoQueContengaX.elTextoContiene(PERFIL),
        ValidarTexto.validarTexto(user.getNombreUsuario()),
        ValidarTextoQueContengaX.elTextoContiene(VERSION));

    EvidenciaUtils.registrarCaptura(paso);

    actor.attemptsTo(ClickTextoQueContengaX.elTextoContiene(MUNDO_CLARO));
  }

  public static Performable validarVersion() {
    return instrumented(VersionSuperApp.class);
  }
}
