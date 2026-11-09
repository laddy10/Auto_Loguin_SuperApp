package runners;

import cucumber.api.CucumberOptions;
import cucumber.api.SnippetType;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

/**
 * Runner de los escenarios de inicio de sesión. El orquestador elige el tag con
 * -Dcucumber.options="--tags @..."; el de aquí es solo el valor por defecto.
 */
@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"stepDefinitions", "hooks"},
        snippets = SnippetType.CAMELCASE,
        plugin = {"pretty"},
        tags = "@LOGIN_COMPLETO"
)
public class GeneralRunner {
}
