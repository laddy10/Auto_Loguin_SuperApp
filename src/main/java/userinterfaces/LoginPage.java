package userinterfaces;

import static utils.Constants.MIENTRAS_APP_ESTA_EN_USO;

import net.serenitybdd.core.annotations.findby.By;
import net.serenitybdd.screenplay.targets.Target;

public class LoginPage {

  public class AppConstants {
    public static final String WHATSAPP_PACKAGE = "com.clarocolombia.miclaro";
    public static final String WHATSAPP_ACTIVITY = "com.claro.superapp.SplashActivity";
  }
  public static final Target LOGO_HOME =
          Target.the("Logo principal en el home de Mi Claro")
                  .located(By.xpath("//android.widget.ImageView[@resource-id=\"com.clarocolombia.miclaro:id/home_logo_iv\"]"));

  public static final Target LOADING_SPLASH =
          Target.the("Logo de espera al iniciar la App Mi Claro")
                  .located(By.id("imgLoadingSplash"));
  public static final Target LBL_ENCABEZADO_USUARIO =
          Target.the("Label Encabezado de usuario").located(By.id("profile_welcome_title"));
  public static final Target LBL_SESION_CERRADA_POR_SEGURIDAD =
          Target.the("Texto Tu sesión se ha cerrado por seguridad")
                  .located(By.xpath("//*[@text='Por seguridad, hemos cerrado tu sesión en Mi Claro. Ingresa de nuevo para administrar tus servicios']"));
  public static final Target LBL_NOS_ALEGRA_TENERTE_DE_VUELTA =
          Target.the("Texto ¡Nos alegra tenerte de vuelta!")
                  .located(By.xpath("//*[@text='¡Nos alegra tenerte de vuelta!']"));
  public static final Target LBL_INICIAR_SESION =
          Target.the("Texto Iniciar sesión")
                  .located(By.id("com.clarocolombia.miclaro:id/tv_login"));
  public static final Target LOADING_ESPERA_UN_MOMENTO =
          Target.the("Logo de espera al iniciar la App Mi Claro")
                  .located(By.id("animation_splash_lottie"));
  public static final Target BTN_ACEPTAR =
          Target.the("Boton Aceptar").located(By.xpath("//*[@text='Aceptar']"));
  public static final Target BTN_PERMISO_UBICACION =
          Target.the("Botón mientras la app está en uso")
                  .located(
                          By.xpath(
                                  "//*[translate(@text, 'ABCDEFGHIJKLMNOPQRSTUVWXYZÁÉÍÓÚ', 'abcdefghijklmnopqrstuvwxyzáéíóú') = '"
                                          + MIENTRAS_APP_ESTA_EN_USO
                                          + "']"));

  public static final Target BTN_ACEPTAR_PERMISO =
          Target.the("Boton Aceptar").located(By.xpath("//*[@text='Aceptar']"));
  public static final Target SMS_PERMISO_LLAMADAS =
          Target.the("Mensaje ¿Permitir que Mi Claro haga y administre las llamadas telefónicas?")
                  .located(
                          By.xpath(
                                  "//*[@text='¿Permitir que Mi Claro haga y administre las llamadas telefónicas?']"));
  public static final Target SMS_PERMISO_NOTIFICACIONES =
          Target.the("Mensaje ¿Permitir que Mi Claro te envíe notificaciones? ")
                  .located(By.xpath("//*[@text='¿Permitir que Mi Claro te envíe notificaciones?']"));
  public static final Target SMS_PERMISO_NOTIFICACIONES2 =
          Target.the("Mensaje ¿Permitir que Mi Claro te envíe notificaciones? ")
                  .located(
                          By.xpath(
                                  "//*[@text='¿Quieres permitir que Mi Claro te envíe notificaciones?']"));
  public static final Target BTN_OMITIR =
          Target.the("Boton de Omitir").located(By.xpath("//*[@text='Omitir']"));
  public static final Target LBL_BIENVENIDA =
          Target.the("Texto ¡Te damos la bienvenida!")
                  .located(By.xpath("//*[@text='¡Te damos la bienvenida!']"));
  public static final Target TXT_PASSWORD =
          Target.the("Ingresar Password").located(By.id("input"));
  public static final Target CHECK_TC =
          Target.the("Check Acepto los términos y condiciones, el tratamiento de mis datos")
                  .located(By.id("iv_protocol"));
  public static final Target TXT_USERNAME =
          Target.the("Ingresar Usuario").located(By.id("input"));
  public static final Target LBL_TERMINOS_Y_CONDICIONES =
          Target.the("Texto términos y condiciones")
                  .located(By.xpath("//*[@text='¡Te damos la bienvenida!']"));
  public static final Target CHECK_TERMINOS_Y_CONDICIONES =
          Target.the("Texto Acepto los términos y condiciones, el tratamiento de mis datos")
                  .located(By.id("iv_protocol"));
  public static final Target LBL_SESION_ABIERTA =
          Target.the("Texto Tienes una sesión abierta en otro dispositivo")
                  .located(By.xpath("//*[@text='Tienes una sesión abierta en otro dispositivo']"));
  public static final Target LBL_INGRESO_BIOMETRICO =
          Target.the("Boton Activar ingreso biometrico")
                  .located(By.xpath("//*[@text='Activa el ingreso biométrico desde tu celular']"));
  public static final Target TXT_AUTORIZACION_VELOCIDAD =
          Target.the("Texto de Autorización de medición de velocidad")
                  .located(By.xpath("//*[@text='Autorización de medición de velocidad']"));

  public static final Target TXT_AUTORIZACION_VELOCIDAD_2 =
          Target.the("Texto Autorización para Medición de Velocidad")
                  .located(By.xpath("//*[@text='Autorización para Medición de Velocidad']"));

  public static final Target LBL_NUMERO_DOCUMENTO =
          Target.the("Texto Ingresa con tu número de documento")
                  .located(By.xpath("//*[@text='Ingresa con tu número de documento']"));

  public static final Target BTN_OTROS_METODOS_INGRESO =
          Target.the("Boton Otros métodos de ingreso")
                  .located(By.xpath("//*[@text='Otros métodos de ingreso']"));

  public static final Target LBL_IDENTIFICADOR_USUARIO =
          Target.the("Texto que identifica la cuenta actual (correo o documento)")
                  .located(By.id("relogin_account"));

  public static final Target BTN_CONTINUAR =
          Target.the("Botón continuar con cuenta activa").located(By.id("login_entry_next"));

  public static final Target BTN_INGRESAR_OTRA_CUENTA =
          Target.the("Botón para ingresar con otra cuenta")
                  .located(By.id("relogin_use_change_account"));

  public static final Target TXT_NO_PERMITIR =
          Target.the("Texto No permitir")
                  .located(By.xpath("//*[contains(text(),'No permitir')]"));

  public static final Target LBL_TUS_SERVICIOS_FAVORITOS =
          Target.the("Label Tus servicios favoritos").located(By.id("card_mini_program_title_tv"));

  // LoginPage.java
  public static final Target LBL_WELCOME_BACK = Target.the("Pantalla Nos alegra tenerte de vuelta")
          .locatedBy("//*[contains(@text,'Nos alegra tenerte de vuelta') or contains(@text,'Te damos la bienvenida a Mi Claro')]");

  public static final Target BTN_CONTINUAR_WELCOME = Target.the("Botón Continuar (welcome back)")
          .locatedBy("//*[contains(@text,'Continuar')]");

  public static final Target LNK_INGRESAR_OTRA_CUENTA = Target.the("Link Ingresar con otra cuenta")
          .locatedBy("//*[contains(@text,'Ingresar con otra cuenta') or contains(@text,'Iniciar sesión con otra cuenta')]");

  public static final Target LNK_VOLVER_PRINCIPAL = Target.the("Volver a la página principal")
          .locatedBy("//*[contains(@text,'Volver a la página principal')]");

  public static final Target TXT_NUMERO =
          Target.the("Ingresar numero o cuenta").located(By.id("input"));

  public static final Target BTN_MENU_HAMBURGUESA =
          Target.the("Boton menu hamburguesa").located(By.id("iv_menu"));




}