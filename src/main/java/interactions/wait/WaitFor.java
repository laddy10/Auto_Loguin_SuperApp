package interactions.wait;

import static net.serenitybdd.screenplay.Tasks.instrumented;

import net.serenitybdd.core.time.InternalSystemClock;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.thucydides.core.annotations.Step;

public class WaitFor implements Interaction {

  private final int var;

  protected WaitFor(int var) {
    this.var = var;
  }

  public static WaitFor aTime(int var) {
    return instrumented(WaitFor.class, var);
  }

  @Override
  @Step("{0}")
  public <T extends Actor> void performAs(T actor) {
    new InternalSystemClock().pauseFor(var);
  }
}