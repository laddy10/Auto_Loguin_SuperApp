package tasks.Login;

import interactions.Click.ClickElementByText;
import interactions.Click.ClickTextoQueContengaX;
import interactions.comunes.Atras;
import interactions.validations.ValidateInformationText;
import interactions.wait.WaitFor;
import interactions.wait.WaitForResponse;
import models.User;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Enter;
import net.serenitybdd.screenplay.actions.Scroll;
import net.serenitybdd.screenplay.matchers.WebElementStateMatchers;
import net.serenitybdd.screenplay.questions.Presence;
import net.serenitybdd.screenplay.waits.WaitUntil;
import net.thucydides.core.steps.StepEventBus;
import utils.AndroidObject;
import utils.CapturaDePantallaMovil;
import utils.EvidenciaUtils;
import utils.MyDriver;
import utils.TablaResultados;
import utils.TestDataProvider;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static net.serenitybdd.screenplay.Tasks.instrumented;
import static org.hamcrest.core.IsEqual.equalTo;
import static userinterfaces.LoginPage.*;
import static utils.Constants.*;

public class LoginSuperApp implements Task {

    public enum Metodo {CORREO, DOCUMENTO, PIN, CAMBIO_CONTRASENA, TODOS}

    private final LoginSuperApp.Metodo metodo;
    private final User user = TestDataProvider.getRealUser();

    public LoginSuperApp(LoginSuperApp.Metodo metodo) {
        this.metodo = metodo;
    }

    @Override
    public <T extends Actor> void performAs(T actor) {

        // Monitoreo completo (@LOGIN_COMPLETO): no cambia. Ejecuta los 4 flujos,
        // tolera fallos y genera la tabla de resultados al final.
        if (metodo == Metodo.TODOS) {
            prepararDesdeInicio(actor);
            loginCompleto(actor);
            return;
        }

        // Tags individuales: cada uno es un escenario independiente.
        // (1) Dejar la app en un estado conocido, (2) hacer el login,
        // (3) validar y cerrar sesión para que el siguiente tag arranque limpio.
        // Aquí NO se capturan excepciones a propósito: si el login falla, el
        // escenario debe quedar FALLIDO para que ST lo registre en su tabla.
        asegurarEstadoInicial(actor);
        cerrarPublicidadSistema(actor);

        switch (metodo) {
            case CAMBIO_CONTRASENA:
                // Este flujo no inicia sesión: regresa solo a la página principal.
                olvideLaContrasenaDocumento(actor);
                break;
            case PIN:
                loginPorPin(actor);
                cerrarPublicidadSistema(actor);
                validarLoginYCerrarSesion(actor, "Login exitoso PIN");
                break;
            case CORREO:
                loginPorCorreo(actor);
                cerrarPublicidadSistema(actor);
                validarLoginYCerrarSesion(actor, "Login exitoso Correo");
                break;
            case DOCUMENTO:
                loginPorDocumento(actor);
                cerrarPublicidadSistema(actor);
                validarLoginYCerrarSesion(actor, "Login exitoso Documento");
                break;
        }
    }

    /**
     * Deja la app en un estado conocido antes de un login individual.
     *
     * Optimización: reiniciar la app cuesta ~50s (cierre + splash), así que solo se
     * hace cuando hace falta. Si el tag anterior terminó bien, la app queda en Home
     * con "Iniciar sesión" y basta con la preparación normal (~2-5s). Si quedó en una
     * pantalla intermedia (porque el tag anterior falló), reiniciar es la única forma
     * fiable de volver al inicio.
     */
    private <T extends Actor> void asegurarEstadoInicial(T actor) {
        // Un popup del sistema puede tapar la pantalla y falsear la detección.
        cerrarPublicidadSistema(actor);
        // Si abrió en "Claro Empresas", pasar a "Personas" antes de evaluar el estado.
        cambiarAPersonasSiEmpresas(actor);

        boolean pantallaConocida =
                AndroidObject.existeRapido(actor, LBL_INICIAR_SESION)
                        || AndroidObject.existeRapido(actor, BTN_MENU_HAMBURGUESA)
                        || AndroidObject.existeRapido(actor, LBL_SESION_CERRADA_POR_SEGURIDAD)
                        || AndroidObject.existeRapido(actor, BTN_CANCELAR_INICIO);

        if (pantallaConocida) {
            prepararDesdeInicio(actor);
        } else {
            reiniciarApp(actor);
        }
    }


    /**
     * Deja la pantalla inicial lista para iniciar sesión: maneja el aviso de
     * "sesión cerrada por seguridad", el botón cancelar y cierra sesión si quedó
     * una abierta. Se usa al inicio y también tras reabrir la app.
     */
    private <T extends Actor> void prepararDesdeInicio(T actor) {
        // (0) Si la app abrió en versión "Claro Empresas", cambiar a "Personas".
        cambiarAPersonasSiEmpresas(actor);

        if (AndroidObject.existeRapido(actor,LBL_SESION_CERRADA_POR_SEGURIDAD)) {
            actor.attemptsTo(
                    ClickTextoQueContengaX.elTextoContiene(INICIAR_SESION)
            );
        }
        if (AndroidObject.existeRapido(actor,BTN_CANCELAR_INICIO)) {
            actor.attemptsTo(
                    ClickTextoQueContengaX.elTextoContiene(CANCELAR)
            );
        }

        // (1) Si hay sesión abierta → cerrar
        asegurarSesionCerrada(actor);
    }

    private <T extends Actor> void asegurarSesionCerrada(T actor) {
        // Si aparece Hola, Gerencia en home → ir a Perfil y cerrar sesión

        if (AndroidObject.existeRapido(actor,BTN_MENU_HAMBURGUESA)) {
            actor.attemptsTo(
                    Click.on(BTN_MENU_HAMBURGUESA)
            );

            cerrarSesion(actor);
        }

    }

    /** Validación estándar usada por @LOGIN_COMPLETO (evidencia "Login exitoso"). */
    private <T extends Actor> void validarLoginYCerrarSesion(T actor) {
        validarLoginYCerrarSesion(actor, "Login exitoso");
    }

    /**
     * Igual que la anterior, pero permite nombrar la evidencia final. Los tags
     * individuales pasan un nombre por método ("Login exitoso PIN", etc.) para que
     * cada uno deje su propia captura aunque compartan la carpeta Capturas/.
     */
    private <T extends Actor> void validarLoginYCerrarSesion(T actor, String nombreEvidencia) {
        // Si aparece el aviso "Tienes una sesión abierta en otro dispositivo", continuar.
        continuarSiSesionAbierta(actor);

        // Home con sesion iniciada. NO se valida con un texto exacto: el front de
        // Mi Claro cambia y "Tus servicios favoritos" dejo de existir, tumbando el
        // flujo. Se acepta cualquiera de varias senales.
        if (!volverAlHome(actor, 45)) {
            throw new AssertionError(
                    "No se llego al Home con sesion iniciada tras el login: no se encontro "
                            + "el menu principal, la seccion de servicios favoritos, ni la "
                            + "pestana \"" + MUNDO_CLARO + "\" para volver al inicio.");
        }

        // Evidencia de login exitoso (nombre fijo: se reescribe si se repite el flujo).
        EvidenciaUtils.registrarEvidencia(nombreEvidencia);

        actor.attemptsTo(
                Click.on(BTN_MENU_HAMBURGUESA)
        );
        actor.should(seeThat(
                ValidateInformationText.validateInformationText(LBL_ENCABEZADO_USUARIO),
                equalTo(user.getNombreUsuario())));
        cerrarSesion(actor);
    }

    /**
     * Espera a que se vea el HOME con la sesion ya iniciada.
     *
     * No depende de un unico texto exacto: el front de Mi Claro cambia con frecuencia
     * y validar con UiSelector().text("Tus servicios favoritos") rompia todo el flujo
     * en cuanto renombraban esa seccion. Se da por bueno el Home si aparece cualquiera
     * de estas senales (de la mas estable a la mas fragil):
     *   1. El menu principal (id iv_menu), que solo existe con sesion iniciada.
     *   2. El contenedor de servicios favoritos (id card_mini_program_title_tv).
     *   3. Un texto que CONTENGA "servicios favoritos" (coincidencia parcial).
     *
     * La validacion definitiva del login sigue siendo el nombre del usuario en el menu.
     * Sondea en silencio para no registrar fallos intermedios en el reporte.
     *
     * Si el front vuelve a cambiar, basta con agregar aqui la nueva senal.
     */
    private <T extends Actor> boolean esperarHomeLogueado(T actor, int segundos) {
        long limite = System.currentTimeMillis() + segundos * 1000L;
        while (System.currentTimeMillis() < limite) {
            if (estaEnHome(actor)) return true;
            if (!dormir(500)) return false;
        }
        return false;
    }

    /** Comprobacion instantanea (sin esperas) de si estamos en el Home logueado. */
    private <T extends Actor> boolean estaEnHome(T actor) {
        return AndroidObject.existeRapido(actor, BTN_MENU_HAMBURGUESA)
                || AndroidObject.existeRapido(actor, LBL_TUS_SERVICIOS_FAVORITOS)
                || AndroidObject.existeTextoRapido(actor, "servicios favoritos");
    }

    /**
     * Deja la app en el Home despues de iniciar sesion, aunque haya redirigido.
     *
     * Mi Claro esta usando redirecciones temporales (fines de semana) que, al iniciar
     * sesion, llevan al usuario a un "mundo" en vez de a la pestana principal: por
     * ejemplo "Entretenimiento". El destino cambia en cada ejercicio, asi que en lugar
     * de reconocer cada uno se NORMALIZA el estado: si no estamos en el Home pero se ve
     * la barra inferior, se pulsa la pestana "Mundo Claro" y con eso se vuelve al inicio.
     *
     * Es idempotente y auto-adaptable: si no hay redireccion, detecta el Home de
     * inmediato y no toca nada. Por eso cuando quiten la redireccion NO hay que
     * cambiar codigo: el flujo se comporta igual que antes.
     */
    private <T extends Actor> boolean volverAlHome(T actor, int segundos) {
        long limite = System.currentTimeMillis() + segundos * 1000L;
        int intentosPestana = 0;

        while (System.currentTimeMillis() < limite) {
            if (estaEnHome(actor)) return true;

            // Redirigido a un mundo: la barra inferior sigue visible.
            if (intentosPestana < 2 && AndroidObject.existeTextoRapido(actor, MUNDO_CLARO)) {
                System.out.println("ℹ️ Login redirigido fuera del Home. Volviendo por la pestana '"
                        + MUNDO_CLARO + "'.");
                try {
                    actor.attemptsTo(ClickTextoQueContengaX.elTextoContiene(MUNDO_CLARO));
                } catch (Throwable ignored) {
                    StepEventBus.getEventBus().clearStepFailures();
                }
                intentosPestana++;
            }

            if (!dormir(700)) return false;
        }
        return false;
    }

    /** Pausa corta; devuelve false si el hilo fue interrumpido. */
    private boolean dormir(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Maneja el aviso "Tienes una sesión abierta en otro dispositivo" que a veces
     * aparece al iniciar sesión y bloquea el flujo: pulsa "Continuar" y espera.
     * Es idempotente: si el aviso no está, no hace nada.
     */
    private <T extends Actor> void continuarSiSesionAbierta(T actor) {
        if (AndroidObject.existeRapido(actor,LBL_SESION_ABIERTA)) {
            actor.attemptsTo(
                    ClickElementByText.clickElementByText(CONTINUAR),
                    WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, WebElementStateMatchers.isNotPresent())
                            .forNoMoreThan(35).seconds()
            );
        }
    }

    private <T extends Actor> void cerrarSesion(T actor) {

        // Click en "Cerrar sesión"
        actor.attemptsTo(
                ClickElementByText.clickElementByText(CERRAR_SESION)
        );

        // Confirmación "Sí, cerrar"
        actor.attemptsTo(
                ClickElementByText.clickElementByText(SI_CERRAR),
                WaitForResponse.withAnyText(INICIAR_SESION),
                WaitUntil.the(LBL_INICIAR_SESION, WebElementStateMatchers.isVisible()).forNoMoreThan(15).seconds()
        );

    }

    private <T extends Actor> void loginPorCorreo(T actor) {
        //EvidenciaUtils.registrarCaptura("Preparar login por CORREO");
        abrirLoginDesdeHome(actor);
        bypassWelcomeBackIfPresent(actor);

        //EvidenciaUtils.registrarCaptura("Pantalla Login Seleccionar Otros metodos de ingreso");
        actor.attemptsTo(
                WaitUntil.the(BTN_OTROS_METODOS_INGRESO, WebElementStateMatchers.isVisible()).forNoMoreThan(15).seconds(),
                Click.on(BTN_OTROS_METODOS_INGRESO)
        );

        //EvidenciaUtils.registrarCaptura("Seleccion Correo electronico");
        actor.attemptsTo(
                ClickElementByText.clickElementByText(CORREO_ELECTRONICO)
        );

        actor.attemptsTo(
                WaitUntil.the(TXT_USERNAME, WebElementStateMatchers.isEnabled()).forNoMoreThan(15).seconds(),
                Click.on(TXT_USERNAME),
                Enter.theValue(user.getEmail()).into(TXT_USERNAME),
                Atras.irAtras(),
                WaitFor.aTime(500)
        );

        EvidenciaUtils.registrarEvidencia("Ingresar con Correo");
        actor.attemptsTo(ClickElementByText.clickElementByText(CONTINUAR));

        actor.attemptsTo(
                WaitUntil.the(TXT_PASSWORD, WebElementStateMatchers.isEnabled()).forNoMoreThan(15).seconds(),
                Enter.theValue(user.getPassword()).into(TXT_PASSWORD)
        );

        actor.attemptsTo(
                ClickElementByText.clickElementByText(CONTINUAR),
                WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, WebElementStateMatchers.isNotPresent()).forNoMoreThan(35).seconds()
        );
        if (AndroidObject.existeRapido(actor,BTN_ACEPTAR)) {
            actor.attemptsTo(
                    ClickElementByText.clickElementByText(ACEPTAR_2),
                    ClickElementByText.clickElementByText(CONTINUAR),
                    WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, WebElementStateMatchers.isNotPresent()).forNoMoreThan(35).seconds()
            );
        }

        // El aviso de sesión en otro dispositivo puede aparecer tras el login.
        continuarSiSesionAbierta(actor);
    }


    private <T extends Actor> void loginPorDocumento(T actor) {
        //EvidenciaUtils.registrarCaptura("Preparar login por DOCUMENTO");
        abrirLoginDesdeHome(actor);
        bypassWelcomeBackIfPresent(actor);

        actor.attemptsTo(
                WaitUntil.the(TXT_USERNAME, WebElementStateMatchers.isEnabled()).forNoMoreThan(15).seconds(),
                Click.on(TXT_USERNAME),
                Enter.theValue(user.getCedula()).into(TXT_USERNAME),
                Atras.irAtras(),
                WaitFor.aTime(500)
        );

        EvidenciaUtils.registrarEvidencia("Ingresar con documento");
        actor.attemptsTo(ClickElementByText.clickElementByText(CONTINUAR));

        actor.attemptsTo(
                WaitUntil.the(TXT_PASSWORD, WebElementStateMatchers.isEnabled()).forNoMoreThan(15).seconds(),
                Enter.theValue(user.getPassword()).into(TXT_PASSWORD)
        );

        actor.attemptsTo(
                ClickElementByText.clickElementByText(CONTINUAR),
                WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, WebElementStateMatchers.isNotPresent()).forNoMoreThan(35).seconds()
        );

        if (AndroidObject.existeRapido(actor,BTN_ACEPTAR)) {
            actor.attemptsTo(
                    ClickElementByText.clickElementByText(ACEPTAR_2),
                    ClickElementByText.clickElementByText(CONTINUAR),
                    WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, WebElementStateMatchers.isNotPresent()).forNoMoreThan(35).seconds()
            );
        }

        // El aviso de sesión en otro dispositivo puede aparecer tras el login.
        continuarSiSesionAbierta(actor);
    }

    private <T extends Actor> void olvideLaContrasenaDocumento(T actor) {
        //EvidenciaUtils.registrarCaptura("Preparar login por DOCUMENTO");
        abrirLoginDesdeHome(actor);
        bypassWelcomeBackIfPresent(actor);

        actor.attemptsTo(
                WaitUntil.the(TXT_USERNAME, WebElementStateMatchers.isEnabled()).forNoMoreThan(15).seconds(),
                Click.on(TXT_USERNAME),
                Enter.theValue(user.getCedula()).into(TXT_USERNAME),
                Atras.irAtras()
        );

        actor.attemptsTo(
                ClickElementByText.clickElementByText(CONTINUAR),
                Click.on(BTN_OLVIDE_CONTRASENA),
                WaitFor.aTime(2000),
                WaitUntil.the(TXT_PASSWORD, WebElementStateMatchers.isPresent()).forNoMoreThan(15).seconds());

        actor.attemptsTo(
                WaitUntil.the(TXT_USERNAME, WebElementStateMatchers.isEnabled()).forNoMoreThan(15).seconds(),
                Click.on(TXT_USERNAME),
                Enter.theValue(user.getCedula()).into(TXT_USERNAME),
                Atras.irAtras(),
                ClickElementByText.clickElementByText(CONTINUAR),
                WaitUntil.the(LBL_CREA_NUEVA_CONTRASENA, WebElementStateMatchers.isPresent()).forNoMoreThan(15).seconds());

        EvidenciaUtils.registrarEvidencia("Olvide la contrasena");
        actor.attemptsTo(
                Click.on(BTN_FLECHA_ATRAS),
                Click.on(BTN_VOLVER_PAGINA_PRINCIPAL)
        );
    }


    private <T extends Actor> void loginPorPin(T actor) {
        //EvidenciaUtils.registrarCaptura("Preparar login por PIN");
        abrirLoginDesdeHome(actor);

        //Manejo pantalla inicio sesión de nuevo
        bypassWelcomeBackIfPresent(actor);

        actor.attemptsTo(
                WaitUntil.the(BTN_OTROS_METODOS_INGRESO, WebElementStateMatchers.isVisible()).forNoMoreThan(15).seconds(),
                Click.on(BTN_OTROS_METODOS_INGRESO)
        );

        //EvidenciaUtils.registrarCaptura("Seleccion PIN");

        actor.attemptsTo(
                ClickElementByText.clickElementByText(PIN),
                WaitUntil.the(TXT_PASSWORD, WebElementStateMatchers.isEnabled()).forNoMoreThan(15).seconds(),
                Enter.theValue(user.getNumero()).into(TXT_NUMERO)
        );

        // Enviar el número: esto dispara el envío del SMS con el código.
        actor.attemptsTo(ClickElementByText.clickElementByText(CONTINUAR));

        // La pantalla del PIN suele aparecer antes que el SMS, pero el aviso del sistema
        // es otra ventana y puede taparla (tv_pin deja de encontrarse). Se espera en
        // SILENCIO: un WaitUntil que expira deja el escenario en rojo aunque se capture
        // la excepción, porque Serenity registra el fallo antes de propagarlo.
        AndroidObject.esperarPresencia(actor, LBL_INGRESA_PIN, 10);

        EvidenciaUtils.registrarEvidencia("Ingresar PIN");

        // Al llegar el SMS, Android pide permiso para que la app lo lea y autocomplete
        // el código: esperar ese aviso y pulsar "Permitir".
        permitirLecturaSmsSiAparece(actor);

        actor.attemptsTo(
                WaitFor.aTime(1000),
                WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, WebElementStateMatchers.isNotPresent()).forNoMoreThan(35).seconds()
        );
        if (AndroidObject.existeRapido(actor,BTN_ACEPTAR)) {
            actor.attemptsTo(
                    ClickElementByText.clickElementByText(ACEPTAR_2),
                    ClickElementByText.clickElementByText(CONTINUAR),
                    WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, WebElementStateMatchers.isNotPresent()).forNoMoreThan(35).seconds()
            );
        }

        if (AndroidObject.existeRapido(actor,LBL_SESION_ABIERTA)) {
            actor.attemptsTo(
                    ClickElementByText.clickElementByText(CONTINUAR),
                    WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, WebElementStateMatchers.isNotPresent()).forNoMoreThan(35).seconds()
            );
        }

    }

    private <T extends Actor> void loginCompleto(T actor) {
        // Carpeta limpia: al final solo deben quedar las 5 evidencias + la tabla.
        CapturaDePantallaMovil.limpiarCapturas();

        List<TablaResultados.Fila> resultados = new ArrayList<>();

        // Orden tal como estaba: Olvidé contraseña -> PIN -> Correo -> Documento.
        // (Olvidé la contraseña ya regresa a Home, por eso no requiere validación extra.)
        ejecutarLogin(actor, "Cambio de Contraseña",
                a -> olvideLaContrasenaDocumento(a), a -> { }, resultados);

        ejecutarLogin(actor, "Login por PIN",
                a -> loginPorPin(a), a -> validarLoginYCerrarSesion(a), resultados);

        ejecutarLogin(actor, "Login Por Correo y Contraseña",
                a -> loginPorCorreo(a), a -> validarLoginYCerrarSesion(a), resultados);

        ejecutarLogin(actor, "Login por Documento",
                a -> loginPorDocumento(a), a -> validarLoginYCerrarSesion(a), resultados);

        // Tabla de resultados como imagen (queda en Capturas/ y en el informe Word).
        // Si algo falla al generarla, se ignora para no tumbar el resto.
        try {
            List<TablaResultados.Fila> filasTabla = ordenarParaTabla(resultados);
            BufferedImage tabla = TablaResultados.crearImagen(filasTabla);
            EvidenciaUtils.registrarImagen("Tabla de resultados del Login Completo", tabla);
        } catch (Throwable t) {
            System.out.println("⚠️ No se pudo generar la tabla de resultados: " + t);
        }
    }

    /**
     * En el login por PIN, Android muestra un diálogo pidiendo consentimiento para que
     * la app lea el SMS con el código OTP y lo autocomplete. El diálogo aparece cuando
     * llega el mensaje (es asíncrono), por eso se espera un momento a que salga y se
     * pulsa "Permitir". Nunca lanza: si no aparece, continúa.
     */
    private <T extends Actor> void permitirLecturaSmsSiAparece(T actor) {
        final long limite = System.currentTimeMillis() + 40_000L; // el SMS puede tardar
        while (System.currentTimeMillis() < limite) {
            try {
                // El aviso es una ventana del sistema: se busca por TEXTO (UiSelector).
                if (AndroidObject.existeTextoRapido(actor, PERMITIR)) {
                    actor.attemptsTo(ClickElementByText.clickElementByText(PERMITIR));
                    StepEventBus.getEventBus().clearStepFailures();
                    return;
                }
                // Si la app ya entró (leyó el SMS sola), no hay nada que permitir.
                if (AndroidObject.existeTextoRapido(actor, TUS_SERVICIOS_FAVORITOS)) {
                    return;
                }
            } catch (Throwable ignored) {
                StepEventBus.getEventBus().clearStepFailures();
            }
            try {
                Thread.sleep(700);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        System.out.println("⚠️ No apareció el aviso para permitir la lectura del SMS.");
    }

    /**
     * Si la app abrió en la versión "Claro Empresas" (pantalla con el botón
     * "Desliza para Personas"), lo pulsa para cambiar a la versión "Personas".
     * Nunca lanza: si no aparece, no hace nada.
     */
    private <T extends Actor> void cambiarAPersonasSiEmpresas(T actor) {
        try {
            if (AndroidObject.existeRapido(actor, BTN_DESLIZA_PARA_PERSONAS)) {
                actor.attemptsTo(
                        Click.on(BTN_DESLIZA_PARA_PERSONAS),
                        WaitFor.aTime(1500)
                );
                StepEventBus.getEventBus().clearStepFailures();
            }
        } catch (Throwable ignored) {
            StepEventBus.getEventBus().clearStepFailures();
        }
    }

    /**
     * Cierra el popup de publicidad del SIM Toolkit (com.android.stk) si está en
     * pantalla, pulsando "Cancelar". Nunca lanza: si no está, no hace nada.
     */
    private <T extends Actor> void cerrarPublicidadSistema(T actor) {
        try {
            if (AndroidObject.existeRapido(actor,SMS_PUBLICIDAD_SISTEMA)) {
                actor.attemptsTo(Click.on(BTN_CANCELAR_PUBLICIDAD_SISTEMA));
                StepEventBus.getEventBus().clearStepFailures();
            }
        } catch (Throwable ignored) {
            StepEventBus.getEventBus().clearStepFailures();
        }
    }

    /** Ordena las filas para la tabla según el ejemplo (PIN, Correo, Documento, Cambio de Contraseña). */
    private List<TablaResultados.Fila> ordenarParaTabla(List<TablaResultados.Fila> resultados) {
        String[] orden = {
                "Login por PIN",
                "Login Por Correo y Contraseña",
                "Login por Documento",
                "Cambio de Contraseña"
        };
        List<TablaResultados.Fila> ordenadas = new ArrayList<>();
        for (String etiqueta : orden) {
            for (TablaResultados.Fila f : resultados) {
                if (f.metodo.equals(etiqueta)) {
                    ordenadas.add(f);
                    break;
                }
            }
        }
        // Por si quedara alguna etiqueta no contemplada, se agrega al final.
        for (TablaResultados.Fila f : resultados) {
            if (!ordenadas.contains(f)) ordenadas.add(f);
        }
        return ordenadas;
    }

    /**
     * Ejecuta UN único intento de login. Si falla, marca el resultado como fallido,
     * cierra y reabre la app para que el siguiente login arranque con un flujo limpio
     * (evita quedar atascado en avisos como "Algo salió mal"). Nunca propaga la
     * excepción: registra el resultado (OK/FALLO) en la tabla y continúa.
     */
    private void ejecutarLogin(Actor actor, String etiqueta,
                               Consumer<Actor> accion, Consumer<Actor> validacion,
                               List<TablaResultados.Fila> resultados) {
        boolean exitoso = false;

        // Resetea el estado de fallo para que los @Step de este login sí se ejecuten.
        StepEventBus.getEventBus().clearStepFailures();
        try {
            cerrarPublicidadSistema(actor);   // por si el STK tapa la pantalla al empezar
            accion.accept(actor);
            cerrarPublicidadSistema(actor);    // por si apareció durante el login
            validacion.accept(actor);
            exitoso = true;
        } catch (Throwable t) {
            System.out.println("⚠️ [" + etiqueta + "] Falló: " + t);
            // Cerrar y reabrir la app para limpiar cualquier estado/aviso colgado.
            reiniciarApp(actor);
        }

        StepEventBus.getEventBus().clearStepFailures();
        String hora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        resultados.add(new TablaResultados.Fila(etiqueta, exitoso, hora, "Android"));
    }

    /**
     * Cierra (fuerza el cierre) y reabre la Super App, espera a que termine el splash
     * y deja la pantalla inicial lista. Nunca lanza: cualquier incidencia se ignora.
     */
    private void reiniciarApp(Actor actor) {
        try {
            StepEventBus.getEventBus().clearStepFailures();
            io.appium.java_client.android.AndroidDriver driver = AndroidObject.androidDriver(actor);
            try {
                driver.terminateApp(MyDriver.APP_PACKAGE);
            } catch (Throwable ignored) {
                StepEventBus.getEventBus().clearStepFailures();
            }
            actor.attemptsTo(WaitFor.aTime(1500));
            try {
                driver.activateApp(MyDriver.APP_PACKAGE);
            } catch (Throwable ignored) {
                StepEventBus.getEventBus().clearStepFailures();
            }

            // Esperar a que termine el splash / loading.
            esperarAppLista(actor);

            // Por si el SIM Toolkit muestra publicidad al relanzar (splash/home).
            cerrarPublicidadSistema(actor);

            // Dejar la pantalla inicial lista (avisos previos + sesión cerrada).
            try {
                prepararDesdeInicio(actor);
            } catch (Throwable ignored) {
                StepEventBus.getEventBus().clearStepFailures();
            }
        } catch (Throwable t) {
            System.out.println("⚠️ No se pudo reiniciar la app: " + t);
            StepEventBus.getEventBus().clearStepFailures();
        }
    }

    /** Espera a que desaparezcan las pantallas de carga tras abrir la app. */
    private void esperarAppLista(Actor actor) {
        try {
            actor.attemptsTo(
                    WaitUntil.the(LOADING_SPLASH, WebElementStateMatchers.isNotPresent()).forNoMoreThan(50).seconds(),
                    WaitUntil.the(LOADING_ESPERA_UN_MOMENTO, WebElementStateMatchers.isNotPresent()).forNoMoreThan(50).seconds(),
                    WaitFor.aTime(1500)
            );
        } catch (Throwable ignored) {
            StepEventBus.getEventBus().clearStepFailures();
        }
    }

    private <T extends Actor> void bypassWelcomeBackIfPresent(T actor) {
        // ¿Está la pantalla intermedia?
        //if (Presence.of(LBL_WELCOME_BACK).viewedBy(actor).resolveAll().isEmpty()) return;


        // 1) Preferir 'Ingresar con otra cuenta' si está visible & habilitado
        if (AndroidObject.existeRapido(actor,LNK_INGRESAR_OTRA_CUENTA)) {
            if (LNK_INGRESAR_OTRA_CUENTA.resolveFor(actor).isClickable() || LNK_INGRESAR_OTRA_CUENTA.resolveFor(actor).isEnabled()) {
                actor.attemptsTo(Click.on(LNK_INGRESAR_OTRA_CUENTA));
                return;
            }
        }

        // 2) Si no hay link, intentar 'Continuar' SOLO si está habilitado
        if (AndroidObject.existeRapido(actor,BTN_CONTINUAR_WELCOME)) {
            // Espera breve a que se habilite; si no, no intentes el click
            actor.attemptsTo(WaitUntil.the(BTN_CONTINUAR_WELCOME, WebElementStateMatchers.isVisible())
                    .forNoMoreThan(5).seconds());
            boolean enabled = false;
            try {
                enabled = BTN_CONTINUAR_WELCOME.resolveFor(actor).isEnabled();
            } catch (Exception ignored) {
            }

            if (enabled) {
                actor.attemptsTo(Click.on(BTN_CONTINUAR_WELCOME));
                // tras continuar, suele aparecer el link; si aparece, clic
                if (AndroidObject.existeRapido(actor,LNK_INGRESAR_OTRA_CUENTA)) {
                    actor.attemptsTo(Click.on(LNK_INGRESAR_OTRA_CUENTA));
                    return;
                }
            }
        }
    }

    private <T extends Actor> void abrirLoginDesdeHome(T actor) {
        // Esperar (corto) a que el home muestre "Iniciar sesión" antes de decidir.
        try {
            actor.attemptsTo(
                    WaitUntil.the(LBL_INICIAR_SESION, WebElementStateMatchers.isVisible()).forNoMoreThan(10).seconds()
            );
        } catch (Throwable ignored) {
            StepEventBus.getEventBus().clearStepFailures();
        }
        if (AndroidObject.existeRapido(actor, LBL_INICIAR_SESION)) {
            actor.attemptsTo(Click.on(LBL_INICIAR_SESION));
        }
    }

    private <T extends Actor> void postLoginHousekeeping(T actor) {
        if (AndroidObject.existeRapido(actor,LBL_INGRESO_BIOMETRICO)) {
            actor.attemptsTo(ClickElementByText.clickElementByText(EN_OTRO_MOMENTO));
        }
        if (AndroidObject.existeRapido(actor,LBL_SESION_ABIERTA)) {
            actor.attemptsTo(ClickElementByText.clickElementByText(CONTINUAR));
        }
        if (AndroidObject.existeRapido(actor,LBL_TERMINOS_Y_CONDICIONES)) {
            actor.attemptsTo(Click.on(CHECK_TERMINOS_Y_CONDICIONES),
                    ClickElementByText.clickElementByText(CONTINUAR));
        }
        if (AndroidObject.existeRapido(actor,LBL_INGRESO_BIOMETRICO)) {
            actor.attemptsTo(
                    ClickElementByText.clickElementByText("En otro momento"));
        }
    }


    public static Performable con(LoginSuperApp.Metodo metodo) {
        return instrumented(LoginSuperApp.class, metodo);
    }
}
