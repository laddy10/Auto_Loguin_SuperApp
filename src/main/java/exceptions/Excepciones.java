package exceptions;

import interactions.wait.WaitFor;
import io.appium.java_client.MobileBy;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.questions.Presence;

import static userinterfaces.LoginPage.TXT_NO_PERMITIR;
import static utils.AndroidObject.androidDriver;

public class Excepciones {

  public void ExClickElTextoContiene(Actor actor, String text) {
    if (!Presence.of(TXT_NO_PERMITIR).viewedBy(actor).resolveAll().isEmpty()) {
      androidDriver(actor).findElement(
                      new MobileBy.ByAndroidUIAutomator("new UiSelector().text(\"No permitir\")"))
              .click();
      actor.attemptsTo(WaitFor.aTime(1000));
    }
    androidDriver(actor).findElement(
                    new MobileBy.ByAndroidUIAutomator("new UiSelector().textContains(\"" + text + "\")"))
            .click();
  }

}