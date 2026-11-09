package userinterfaces;

import static userinterfaces.Elemento.porId;
import static userinterfaces.Elemento.porTexto;
import static userinterfaces.Elemento.porTextoQueContiene;
import static userinterfaces.Elemento.porTextoQueCumple;
import static userinterfaces.Elemento.porUiSelector;

/**
 * Elementos de Mi Claro usados por los flujos de inicio de sesión (tasks.Login.FlujosLogin).
 *
 * Búsqueda rápida por id o UiSelector, sin Serenity (ver Elemento).
 */
public final class ElementosLogin {

    private ElementosLogin() {
    }

    // ── Carga de la app ──────────────────────────────────────────────────────
    public static final Elemento SPLASH =
            porId("el logo de carga de Mi Claro", "imgLoadingSplash");
    public static final Elemento CARGANDO =
            porId("la animación \"Espera un momento\"", "animation_splash_lottie");

    // ── Inicio sin sesión ────────────────────────────────────────────────────
    public static final Elemento INICIAR_SESION =
            porId("el botón \"Iniciar sesión\" del inicio", "tv_login");

    // ── Formulario de inicio de sesión ──────────────────────────────────────
    public static final Elemento CAMPO_TEXTO =
            porId("el campo de texto del formulario", "input");
    public static final Elemento OTROS_METODOS =
            porTexto("el botón \"Otros métodos de ingreso\"", "Otros métodos de ingreso");
    public static final Elemento OPCION_CORREO =
            porTexto("la opción \"Correo electrónico\"", "Correo electrónico");
    public static final Elemento OPCION_PIN =
            porTexto("la opción \"PIN\"", "PIN");
    public static final Elemento CONTINUAR =
            porTexto("el botón \"Continuar\"", "Continuar");
    public static final Elemento OLVIDE_CONTRASENA =
            porId("el enlace \"Olvidé mi contraseña\"", "tv_lose_pass");
    public static final Elemento CREA_NUEVA_CONTRASENA =
            porTexto("la pantalla \"Crea tu nueva contraseña\"", "Crea tu nueva contraseña");
    public static final Elemento FLECHA_ATRAS =
            porId("la flecha para volver", "iv_back");
    public static final Elemento VOLVER_PAGINA_PRINCIPAL =
            porId("el botón \"Volver a la página principal\"", "tv_guest_page");

    // ── PIN por SMS ──────────────────────────────────────────────────────────
    public static final Elemento PANTALLA_PIN =
            porId("la pantalla para ingresar el PIN", "tv_pin");
    /** Aviso de Android (SMS User Consent) para que la app lea el SMS con el código. */
    public static final Elemento PERMITIR_LEER_SMS =
            porTexto("el botón \"Permitir\" del aviso para leer el SMS", "Permitir");

    // ── Inicio con sesión iniciada ───────────────────────────────────────────
    /** Solo existe con la sesión iniciada: es la señal más estable del Home. */
    public static final Elemento MENU_PRINCIPAL =
            porId("el menú principal", "iv_menu");
    public static final Elemento SERVICIOS_FAVORITOS =
            porId("la sección de servicios favoritos", "card_mini_program_title_tv");
    public static final Elemento TEXTO_SERVICIOS_FAVORITOS =
            porTextoQueContiene("el texto \"servicios favoritos\"", "servicios favoritos");
    public static final Elemento PESTANA_MUNDO_CLARO =
            porTextoQueContiene("la pestaña \"Mundo Claro\"", "Mundo Claro");
    public static final Elemento SALUDO_PERFIL =
            porId("el saludo del perfil", "profile_welcome_title");
    public static final Elemento CERRAR_SESION =
            porTexto("la opción \"Cerrar sesión\"", "Cerrar sesión");
    public static final Elemento SI_CERRAR =
            porTexto("el botón \"Si, cerrar\"", "Si, cerrar");

    // ── Pantallas que interrumpen el flujo normal (desvíos) ─────────────────
    public static final Elemento SESION_ABIERTA_OTRO_DISPOSITIVO =
            porTextoQueContiene("el aviso \"Tienes una sesión abierta en otro dispositivo\"",
                    "sesión abierta en otro dispositivo");
    public static final Elemento SESION_CERRADA_POR_SEGURIDAD =
            porTextoQueContiene("el aviso \"Por seguridad, hemos cerrado tu sesión\"",
                    "hemos cerrado tu sesión");
    public static final Elemento BOTON_INFERIOR =
            porId("el botón inferior del aviso", "bt_bottom");
    public static final Elemento CANCELAR =
            porTexto("el botón \"Cancelar\"", "Cancelar");
    public static final Elemento TEXTO_INICIAR_SESION =
            porTextoQueContiene("el botón \"Iniciar sesión\"", "Iniciar sesión");
    /** Pantalla "Nos alegra tenerte de vuelta": la app recuerda la última cuenta. */
    public static final Elemento INGRESAR_CON_OTRA_CUENTA =
            porTextoQueCumple("el enlace \"Ingresar con otra cuenta\"",
                    "(?i).*(Ingresar|Iniciar sesión) con otra cuenta.*");
    public static final Elemento DESLIZA_PARA_PERSONAS =
            porTextoQueContiene("el botón \"Desliza para Personas\" (versión Empresas)",
                    "Desliza para Personas");
    public static final Elemento CERRAR_MINIPROGRAMA =
            porUiSelector("el botón para cerrar el miniprograma",
                    "new UiSelector().className(\"android.widget.Button\").description(\"Cerrar\")");
    public static final Elemento PUBLICIDAD_SIM =
            porUiSelector("la publicidad del SIM Toolkit",
                    "new UiSelector().packageName(\"com.android.stk\")");
    public static final Elemento CANCELAR_PUBLICIDAD_SIM =
            porId("el botón Cancelar de la publicidad del SIM Toolkit", "android:id/button2");
    public static final Elemento ACEPTAR =
            porTexto("el botón \"Aceptar\"", "Aceptar");
    public static final Elemento INGRESO_BIOMETRICO =
            porTextoQueContiene("el aviso para activar el ingreso biométrico",
                    "Activa el ingreso biométrico");
    public static final Elemento EN_OTRO_MOMENTO =
            porTexto("el botón \"En otro momento\"", "En otro momento");
    public static final Elemento BIENVENIDA_TERMINOS =
            porTexto("la bienvenida con términos y condiciones", "¡Te damos la bienvenida!");
    public static final Elemento CHECK_TERMINOS =
            porId("la casilla de términos y condiciones", "iv_protocol");
}
