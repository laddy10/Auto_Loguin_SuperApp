package interactions.Click;

import static net.serenitybdd.screenplay.Tasks.instrumented;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.thucydides.core.annotations.Step;
import utils.AndroidObject;

public class ClickTextoQueContengaX extends AndroidObject implements Interaction {

  private String text;

  public ClickTextoQueContengaX(String text) {
    this.text = text;
  }

  @Override
  @Step("Valida que el texto '#text' este contenido en otro texto visible.")
  public <T extends Actor> void performAs(T actor) {
    ClickElTextoContiene(actor, text);
  }

  public static Interaction elTextoContiene(String text) {
    return instrumented(ClickTextoQueContengaX.class, text);
  }
}