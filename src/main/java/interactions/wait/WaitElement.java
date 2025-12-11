package interactions.wait;

import static net.serenitybdd.screenplay.Tasks.instrumented;

import interactions.comunes.Enable;
import interactions.comunes.Visible;
import net.serenitybdd.screenplay.targets.Target;

public class WaitElement {

  private WaitElement() {}

  public static Visible isVisible(Target element) {
    return instrumented(Visible.class, element);
  }

  public static Enable isEnable(Target element) {
    return instrumented(Enable.class, element);
  }
}
