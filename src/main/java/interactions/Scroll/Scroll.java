package interactions.Scroll;

import static net.serenitybdd.screenplay.Tasks.instrumented;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.targets.Target;
import net.thucydides.core.annotations.Step;
import utils.AndroidObject;

public class Scroll extends AndroidObject implements Interaction {

  public static Performable to(Target xpath) {
    return instrumented(Scroll.class);
  }

  @Override
  @Step("Realiza Scroll una vez hacia abajo")
  public <T extends Actor> void performAs(T actor) {
    UnScrollAbajo(actor);
  }

  public static Interaction scrollUnaVista() {
    return instrumented(Scroll.class);
  }
}
