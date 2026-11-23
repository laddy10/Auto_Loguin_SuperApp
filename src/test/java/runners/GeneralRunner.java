package runners;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Runner de los escenarios de inicio de sesión. El tag lo elige build.gradle
 * (cucumber.filter.tags) a partir de -Pcucumber.tags o -Dcucumber.options.
 *
 * Reporter de Serenity NO paralelo a propósito: utils.Paso abre pasos propios en el
 * StepEventBus a mitad de un paso de Cucumber, y eso solo queda en orden si los
 * eventos se procesan en vivo (el paralelo los acumula y los reproduce al final).
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "stepDefinitions,hooks")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME,
        value = "pretty,net.serenitybdd.cucumber.core.plugin.SerenityReporter")
@ConfigurationParameter(key = Constants.SNIPPET_TYPE_PROPERTY_NAME, value = "camelcase")
public class GeneralRunner {
}
