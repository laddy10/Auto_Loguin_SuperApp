package utils;

import io.appium.java_client.android.AndroidDriver;
import net.thucydides.core.webdriver.DriverSource;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;
import java.util.concurrent.TimeUnit;

public class MyDriver implements DriverSource {

  public static final String APP_PACKAGE = "com.clarocolombia.miclaro";

  // Espera implícita global. Antes era 20s, lo que hacía que cada verificación de
  // pantalla ausente (Presence.of(...)) bloqueara 20s. Se baja para acelerar; la
  // sincronización real se hace con esperas explícitas (WaitUntil...forNoMoreThan).
  public static final int IMPLICIT_WAIT_SECONDS = 5;

  private static AndroidDriver driver;

  public static AndroidDriver getDriver() {
    return driver;
  }

  @Override
  public WebDriver newDriver() {
    try {
      DesiredCapabilities caps = new DesiredCapabilities();

      // Capabilities base (alineadas a serenity.properties)
      caps.setCapability("automationName", "UiAutomator2");
      caps.setCapability("platformName", "Android");
      caps.setCapability("app", System.getProperty("app", System.getProperty("user.dir") + "/src/test/resources/app/mi-claro.apk"));
      caps.setCapability("appPackage", APP_PACKAGE);
      caps.setCapability("appActivity", "com.claro.superapp.SplashActivity");

      caps.setCapability("noReset", true);
      caps.setCapability("fullReset", false);
      caps.setCapability("autoGrantPermissions", true);
      caps.setCapability("autoDismissAlerts", true);
      caps.setCapability("newCommandTimeout", 8000);
      // Si usas WebView y necesitas ChromeDriver:
      // caps.setCapability("chromedriverExecutable", "src/test/resources/webdriver/windows/chromedriver.exe");

      URL hub = new URL("http://127.0.0.1:4723/wd/hub");
      driver = new AndroidDriver(hub, caps);
      driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT_SECONDS, TimeUnit.SECONDS);
      return driver;

    } catch (Exception e) {
      throw new RuntimeException("Error iniciando el driver de Appium", e);
    }
  }

  @Override
  public boolean takesScreenshots() {
    return true;
  }
}