package runners;

import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import utils.BeforeSuite;
import utils.DataToFeature;
import utils.SeleniumFunctions;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Properties;

public class CustomRunner extends Runner {

  private Class<CucumberWithSerenity> classValue;
  private CucumberWithSerenity cucumberWithSerenity;
  private final Properties prop = new Properties();
  SeleniumFunctions functions = new SeleniumFunctions();
  private final InputStream in = SeleniumFunctions.class.getResourceAsStream("/test.properties");

  public CustomRunner(Class<CucumberWithSerenity> classValue) throws Exception {
    this.classValue = classValue;
    cucumberWithSerenity = new CucumberWithSerenity(classValue);
  }

  public String readProperties(String property) throws IOException {
    prop.load(in);
    return prop.getProperty(property);
  }

  @Override
  public Description getDescription() {
    return cucumberWithSerenity.getDescription();
  }

  private void runAnnotatedMethods(Class<?> annotation) throws Exception {
    if (!annotation.isAnnotation()) {
      return;
    }
    Method[] methods = this.classValue.getMethods();
    for (Method method : methods) {
      Annotation[] annotations = method.getAnnotations();
      for (Annotation item : annotations) {
        if (item.annotationType().equals(annotation)) {
          method.invoke(null);
          break;
        }
      }
    }
  }

  @Override
  public void run(RunNotifier notifier) {
    try {
      String environment = readProperties("Environment");
      functions.saveInScenario("Environment", environment);

      DataToFeature.backUpFeaturesFile();
      runAnnotatedMethods(BeforeSuite.class);
      cucumberWithSerenity = new CucumberWithSerenity(classValue);
    } catch (Exception e) {
      e.printStackTrace();
    }
    cucumberWithSerenity.run(notifier);
    DataToFeature.restoreBackUpFeatures();
  }
}