package tasks.Login;

import io.appium.java_client.android.AndroidDriver;
import models.User;
import net.serenitybdd.screenplay.Actor;
import org.openqa.selenium.WebDriverException;
import userinterfaces.Elemento;
import utils.EvidenciaUtils;
import utils.Enmascarar;
import utils.Pantalla;
import utils.Pantalla.Desvio;
import utils.Paso;
import utils.TestDataProvider;

import java.util.Arrays;
import java.util.List;

import static userinterfaces.ElementosLogin.*;

/**
 * Flujos de inicio de sesión de Mi Claro: cambio de contraseña, PIN, correo y documento.
 *
 * Estrategia: CAMINO FELIZ PRIMERO. Cada paso espera la pantalla que el flujo normal
 * debería mostrar; solo si no aparece en un momento se revisan los desvíos conocidos
 * (sesión abierta en otro dispositivo, versión Empresas, miniprogramas, publicidad del
 * SIM...). Antes se revisaban todos los desvíos uno por uno ANTES de seguir, y cada
 * revisión de algo ausente costaba ~10 s: más de un minuto por inicio de sesión aunque
 * no apareciera ninguno.
 *
 * Reporte: cada acción de negocio es un paso en español (utils.Paso) con su evidencia;
 * los clics internos no generan pasos y la contraseña nunca se escribe en el reporte.
 * Si se atiende un desvío, queda como un paso más ("La app avisó que...").
 *
 * Todos los flujos parten del inicio sin sesión y terminan en él (cerrando sesión), para
 * que el siguiente arranque limpio. Si algo falla, el error dice qué faltó y qué había
 * en pantalla.
 */
public class FlujosLogin {

    /** Señales del Home con la sesión iniciada (de la más estable a la más frágil). */
    private static final List<Elemento> HOME_CON_SESION =
            Arrays.asList(MENU_PRINCIPAL, SERVICIOS_FAVORITOS, TEXTO_SERVICIOS_FAVORITOS);

    /** Primera pantalla tras pulsar "Iniciar sesión" (normal o "Nos alegra tenerte de vuelta"). */
    private static final List<Elemento> FORMULARIO_O_BIENVENIDA =
            Arrays.asList(INGRESAR_CON_OTRA_CUENTA, OTROS_METODOS, CAMPO_TEXTO);

    private static final List<Elemento> FORMULARIO =
            Arrays.asList(OTROS_METODOS, CAMPO_TEXTO);

    /** Tiempo máximo para que llegue el SMS con el PIN (en las corridas buenas tarda ~10 s). */
    private static final int ESPERA_SMS_SEGUNDOS = 60;

    private final Actor actor;
    private final User user = TestDataProvider.getRealUser();

    public FlujosLogin(Actor actor) {
        this.actor = actor;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Apertura de la app
    // ═════════════════════════════════════════════════════════════════════════

    public void esperarQueCargueLaApp() {
        Paso.ejecutar("Esperar a que cargue la app Mi Claro", () -> {
            if (!Pantalla.esperarQueDesaparezca(actor, SPLASH, 60)) {
                throw new AssertionError("La app Mi Claro no terminó de cargar: el logo de carga"
                        + " sigue en pantalla después de 60 s." + Pantalla.describirPantalla(actor));
            }
            if (!Pantalla.esperarQueDesaparezca(actor, CARGANDO, 45)) {
                throw new AssertionError("La app Mi Claro no terminó de cargar: \"Espera un momento\""
                        + " sigue en pantalla después de 45 s." + Pantalla.describirPantalla(actor));
            }
            Pantalla.dormir(1000);
        });
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Flujos
    // ═════════════════════════════════════════════════════════════════════════

    /** Olvidé mi contraseña con el documento: llega a "Crea tu nueva contraseña" y vuelve al inicio. */
    public void cambioDeContrasena() {
        abrirInicioDeSesion();

        String documento = user.getCedula();
        Paso.ejecutar("Ingresar el documento " + Enmascarar.documento(documento)
                + " y elegir \"Olvidé mi contraseña\"", () -> {
            Pantalla.escribir(actor, CAMPO_TEXTO, documento, 15);
            Pantalla.ocultarTeclado(actor);
            Pantalla.tocar(actor, CONTINUAR, 10);
            capturarYTocar(OLVIDE_CONTRASENA, 15);
        });

        Paso.ejecutar("Confirmar el documento para recuperar la contraseña", () -> {
            // El campo de la pantalla de recuperación tiene el mismo id que el de la
            // contraseña: se espera a que la pantalla anterior se vaya antes de escribir.
            Pantalla.esperarQueDesaparezca(actor, OLVIDE_CONTRASENA, 10);
            Pantalla.escribir(actor, CAMPO_TEXTO, documento, 15);
            Pantalla.ocultarTeclado(actor);
            capturarYTocar(CONTINUAR, 10);
        });

        Paso.ejecutar("Validar la pantalla \"Crea tu nueva contraseña\"", () -> {
            if (!Pantalla.esperar(actor, CREA_NUEVA_CONTRASENA, 20)) {
                throw Pantalla.noAparecio(actor, CREA_NUEVA_CONTRASENA, 20);
            }
            EvidenciaUtils.registrarEvidencia("Olvide la contrasena");
        });

        Paso.ejecutar("Volver a la página principal", () -> {
            Pantalla.tocar(actor, FLECHA_ATRAS, 10);
            Pantalla.tocar(actor, VOLVER_PAGINA_PRINCIPAL, 10);
            // No se exige el inicio aquí: el siguiente flujo lo verifica y lo recupera.
            Pantalla.esperar(actor, INICIAR_SESION, 15);
        });
    }

    /** Ingreso con PIN: número de celular → SMS con el código → la app lo lee sola. */
    public void loginPorPin(String evidenciaFinal) {
        abrirInicioDeSesion();

        Paso.ejecutar("Elegir el método PIN e ingresar el celular "
                + Enmascarar.celular(user.getNumero()), () -> {
            Pantalla.tocar(actor, OTROS_METODOS, 15);
            Pantalla.tocar(actor, OPCION_PIN, 10);
            Pantalla.escribir(actor, CAMPO_TEXTO, user.getNumero(), 15);
            capturarYTocar(CONTINUAR, 10);
        });

        Paso.ejecutar("Validar que la app pide el PIN enviado por SMS", () -> {
            // Si el SMS llega rápido, el aviso de Android para leerlo (otra ventana) tapa
            // la pantalla del PIN y esta deja de encontrarse: cualquiera de las dos sirve.
            Elemento visto = Pantalla.esperarAlguno(actor, 20,
                    Arrays.asList(PANTALLA_PIN, PERMITIR_LEER_SMS), desvioPublicidadSim());
            if (visto == null) {
                throw Pantalla.noAparecio(actor, PANTALLA_PIN, 20);
            }
            EvidenciaUtils.registrarEvidencia("Ingresar PIN");
        });

        Paso.ejecutar("Esperar el SMS con el PIN y permitir que la app lo lea", () -> {
            // Al llegar el SMS, Android pregunta si la app puede leerlo y autocompletar el
            // código. Si la app lo lee sola (sin preguntar), pasa directo al Home.
            Elemento visto = Pantalla.esperarAlguno(actor, ESPERA_SMS_SEGUNDOS,
                    Arrays.asList(PERMITIR_LEER_SMS, MENU_PRINCIPAL), desvioPublicidadSim());
            if (visto == null) {
                throw new AssertionError("No llegó el SMS con el PIN: en " + ESPERA_SMS_SEGUNDOS
                        + " s no apareció el aviso de Android para leer el código y la app sigue"
                        + " esperando el PIN." + Pantalla.describirPantalla(actor));
            }
            if (visto == PERMITIR_LEER_SMS) {
                capturarYTocar(PERMITIR_LEER_SMS, 5);
            }
        });

        validarSesionIniciadaYCerrar(evidenciaFinal);
    }

    /** Ingreso con correo electrónico y contraseña. */
    public void loginPorCorreo(String evidenciaFinal) {
        abrirInicioDeSesion();

        Paso.ejecutar("Elegir el método correo electrónico", () -> {
            Pantalla.tocar(actor, OTROS_METODOS, 15);
            Pantalla.tocar(actor, OPCION_CORREO, 10);
            Pantalla.esperar(actor, CAMPO_TEXTO, 10);
        });

        String correo = user.getEmail();
        Paso.ejecutar("Ingresar el correo " + Enmascarar.correo(correo), () -> {
            Pantalla.escribir(actor, CAMPO_TEXTO, correo, 15);
            Pantalla.ocultarTeclado(actor);
            EvidenciaUtils.registrarEvidencia("Ingresar con Correo");
            Pantalla.tocar(actor, CONTINUAR, 10);
        });

        ingresarContrasena(correo);
        validarSesionIniciadaYCerrar(evidenciaFinal);
    }

    /** Ingreso con número de documento y contraseña. */
    public void loginPorDocumento(String evidenciaFinal) {
        abrirInicioDeSesion();

        String documento = user.getCedula();
        Paso.ejecutar("Ingresar el documento " + Enmascarar.documento(documento), () -> {
            Pantalla.escribir(actor, CAMPO_TEXTO, documento, 15);
            Pantalla.ocultarTeclado(actor);
            EvidenciaUtils.registrarEvidencia("Ingresar con documento");
            Pantalla.tocar(actor, CONTINUAR, 10);
        });

        ingresarContrasena(documento);
        validarSesionIniciadaYCerrar(evidenciaFinal);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Partes comunes
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Deja la app en el inicio sin sesión, pulsa "Iniciar sesión" y llega al formulario.
     *
     * Camino feliz: el flujo anterior terminó en el inicio, así que "Iniciar sesión" ya
     * está en pantalla y esto toma un par de segundos. Si no está, se atienden los
     * desvíos conocidos; si la pantalla no se reconoce, se reabre la app.
     */
    private void abrirInicioDeSesion() {
        Paso.ejecutar("Abrir la pantalla de inicio de sesión", () -> {
            if (!Pantalla.esperar(actor, INICIAR_SESION, 10, desviosDelInicio())) {
                Paso.ejecutar("Reabrir la app: quedó en una pantalla que no se reconoce", () -> {
                    Paso.capturar(); // la pantalla desconocida, antes de cerrar la app
                    reabrirApp();
                });
                if (!Pantalla.esperar(actor, INICIAR_SESION, 25, desviosDelInicio())) {
                    throw new AssertionError("La app no muestra el inicio con el botón"
                            + " \"Iniciar sesión\", ni siquiera después de reabrirla."
                            + Pantalla.describirPantalla(actor));
                }
            }
            Pantalla.tocar(actor, INICIAR_SESION, 5);

            Elemento visto = Pantalla.esperarAlguno(actor, 20, FORMULARIO_O_BIENVENIDA,
                    desvioPublicidadSim());
            if (visto == null) {
                throw new AssertionError("Después de pulsar \"Iniciar sesión\" no apareció el"
                        + " formulario de ingreso en 20 s." + Pantalla.describirPantalla(actor));
            }
            if (visto == INGRESAR_CON_OTRA_CUENTA) {
                Paso.ejecutar("La app recordó la última cuenta: se elige \"Ingresar con otra cuenta\"",
                        () -> capturarYTocar(INGRESAR_CON_OTRA_CUENTA, 5));
                if (Pantalla.esperarAlguno(actor, 15, FORMULARIO) == null) {
                    throw new AssertionError("Después de \"Ingresar con otra cuenta\" no apareció el"
                            + " formulario de ingreso en 15 s." + Pantalla.describirPantalla(actor));
                }
            }
        });
    }

    /**
     * @param valorAnterior lo que se escribió en la pantalla anterior (correo o documento).
     *                      El campo de la contraseña tiene el mismo id que el anterior: se
     *                      espera a que ya no muestre ese valor para no escribir la
     *                      contraseña en la pantalla que se está yendo.
     */
    private void ingresarContrasena(String valorAnterior) {
        Paso.ejecutar("Ingresar la contraseña", () -> {
            if (!esperarPantallaDeContrasena(valorAnterior, 15)) {
                throw new AssertionError("No apareció la pantalla para ingresar la contraseña en 15 s."
                        + Pantalla.describirPantalla(actor));
            }
            Pantalla.escribir(actor, CAMPO_TEXTO, user.getPassword(), 10);
            capturarYTocar(CONTINUAR, 10);
        });
    }

    private boolean esperarPantallaDeContrasena(String valorAnterior, int segundos) {
        String anterior = soloLetrasYNumeros(valorAnterior);
        long limite = System.currentTimeMillis() + segundos * 1000L;
        while (System.currentTimeMillis() < limite) {
            if (Pantalla.esta(actor, OLVIDE_CONTRASENA)) {
                return true;
            }
            String enCampo = Pantalla.textoSiEsta(actor, CAMPO_TEXTO);
            if (enCampo != null && !soloLetrasYNumeros(enCampo).contains(anterior)) {
                return true;
            }
            Pantalla.dormir(300);
        }
        return false;
    }

    private static String soloLetrasYNumeros(String texto) {
        return texto == null ? "" : texto.replaceAll("[^\\p{L}\\p{N}@]", "").toLowerCase();
    }

    /**
     * Tras ingresar: espera el Home con la sesión iniciada (atendiendo los avisos que
     * a veces salen), deja la evidencia, valida el nombre del usuario y cierra sesión.
     */
    private void validarSesionIniciadaYCerrar(String evidencia) {
        Paso.ejecutar("Validar que la sesión quedó iniciada", () -> {
            Elemento visto = Pantalla.esperarAlguno(actor, 60, HOME_CON_SESION, desviosTrasIngresar());
            if (visto == null) {
                throw new AssertionError("No se llegó al inicio con la sesión iniciada en 60 s: no"
                        + " aparecieron el menú principal ni la sección de servicios favoritos."
                        + Pantalla.describirPantalla(actor));
            }
            EvidenciaUtils.registrarEvidencia(evidencia);
        });

        String esperado = user.getNombreUsuario() == null ? "" : user.getNombreUsuario().trim();
        Paso.ejecutar("Validar el saludo del usuario en el menú (\"" + esperado + "\")", () -> {
            Pantalla.tocar(actor, MENU_PRINCIPAL, 10);
            String saludo = Pantalla.texto(actor, SALUDO_PERFIL, 10);
            if (!saludo.equals(esperado)) {
                throw new AssertionError("El menú muestra el saludo \"" + saludo
                        + "\" y se esperaba \"" + esperado + "\".");
            }
        });

        cerrarSesion();
    }

    /**
     * Espera el elemento, adjunta la captura al paso (la pantalla justo antes de la
     * acción, que es lo que explica el paso) y lo pulsa.
     */
    private void capturarYTocar(Elemento elemento, int segundos) {
        if (!Pantalla.esperar(actor, elemento, segundos)) {
            throw Pantalla.noAparecio(actor, elemento, segundos);
        }
        Paso.capturar();
        Pantalla.tocar(actor, elemento, 5);
    }

    private void cerrarSesion() {
        Paso.ejecutar("Cerrar sesión", this::cerrarSesionSinPaso);
    }

    private void cerrarSesionSinPaso() {
        if (!Pantalla.esta(actor, CERRAR_SESION)) {
            Pantalla.tocar(actor, MENU_PRINCIPAL, 10);
        }
        Pantalla.tocar(actor, CERRAR_SESION, 10);
        Pantalla.tocar(actor, SI_CERRAR, 10);
        if (!Pantalla.esperar(actor, INICIAR_SESION, 30)) {
            throw new AssertionError("Después de cerrar sesión la app no volvió al inicio:"
                    + " \"Iniciar sesión\" no apareció en 30 s." + Pantalla.describirPantalla(actor));
        }
    }

    /**
     * Cierra y vuelve a abrir la app (para salir de una pantalla desconocida o de un
     * aviso colgado). No falla: si algo no responde, el paso siguiente lo dirá.
     */
    public void reabrirApp() {
        AndroidDriver<?> driver = Pantalla.driver(actor);
        try {
            driver.terminateApp(Elemento.PAQUETE);
        } catch (WebDriverException e) {
            System.out.println("⚠️ No se pudo cerrar la app: " + e.getMessage());
        }
        Pantalla.dormir(1500);
        try {
            driver.activateApp(Elemento.PAQUETE);
        } catch (WebDriverException e) {
            System.out.println("⚠️ No se pudo abrir la app: " + e.getMessage());
        }
        Pantalla.dormir(1500);
        Pantalla.esperarQueDesaparezca(actor, SPLASH, 50);
        Pantalla.esperarQueDesaparezca(actor, CARGANDO, 50);
        Pantalla.dormir(1000);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Desvíos conocidos
    // ═════════════════════════════════════════════════════════════════════════

    /** Lo que puede tapar el inicio sin sesión. */
    private Desvio[] desviosDelInicio() {
        return new Desvio[]{
                Desvio.si(SESION_CERRADA_POR_SEGURIDAD,
                        "La app avisó que cerró la sesión por seguridad (se inició sesión en otro"
                                + " dispositivo): se pulsa \"Iniciar sesión\"",
                        a -> Pantalla.tocar(a, TEXTO_INICIAR_SESION, 5)),
                Desvio.si(MENU_PRINCIPAL,
                        "Había una sesión iniciada: se cierra para empezar desde cero",
                        a -> cerrarSesionSinPaso()).maximo(1),
                Desvio.si(a -> Pantalla.esta(a, BOTON_INFERIOR) && Pantalla.esta(a, CANCELAR),
                        "Se cancela el aviso de sesión en este dispositivo",
                        a -> Pantalla.tocar(a, CANCELAR, 5)),
                Desvio.si(DESLIZA_PARA_PERSONAS,
                        "La app abrió en la versión Empresas: se cambia a Personas",
                        a -> {
                            Pantalla.tocar(a, DESLIZA_PARA_PERSONAS, 5);
                            Pantalla.dormir(1500);
                        }),
                Desvio.si(CERRAR_MINIPROGRAMA,
                        "Se cierra un miniprograma que quedó abierto",
                        a -> Pantalla.tocar(a, CERRAR_MINIPROGRAMA, 5)),
                desvioPublicidadSim(),
                Desvio.si(a -> Pantalla.esta(a, OTROS_METODOS) || Pantalla.esta(a, CAMPO_TEXTO)
                                || Pantalla.esta(a, INGRESAR_CON_OTRA_CUENTA),
                        "La app quedó en el formulario de ingreso: se vuelve atrás al inicio",
                        a -> {
                            Pantalla.driver(a).navigate().back();
                            Pantalla.dormir(1000);
                        }).maximo(3),
        };
    }

    /** Lo que puede aparecer entre la contraseña (o el PIN) y el Home. */
    private Desvio[] desviosTrasIngresar() {
        return new Desvio[]{
                Desvio.si(SESION_ABIERTA_OTRO_DISPOSITIVO,
                        "La app avisó que hay una sesión abierta en otro dispositivo: se pulsa \"Continuar\"",
                        a -> Pantalla.tocar(a, CONTINUAR, 5)),
                Desvio.si(ACEPTAR,
                        "Se acepta el aviso que apareció al ingresar",
                        a -> {
                            Pantalla.tocar(a, ACEPTAR, 5);
                            if (Pantalla.esperar(a, CONTINUAR, 3)) {
                                Pantalla.tocar(a, CONTINUAR, 2);
                            }
                        }),
                Desvio.si(CERRAR_MINIPROGRAMA,
                        "Se cierra un miniprograma que se abrió al ingresar",
                        a -> Pantalla.tocar(a, CERRAR_MINIPROGRAMA, 5)),
                Desvio.si(INGRESO_BIOMETRICO,
                        "Se pospone la activación del ingreso biométrico (\"En otro momento\")",
                        a -> Pantalla.tocar(a, EN_OTRO_MOMENTO, 5)),
                Desvio.si(BIENVENIDA_TERMINOS,
                        "Se aceptan los términos y condiciones de la bienvenida",
                        a -> {
                            Pantalla.tocar(a, CHECK_TERMINOS, 5);
                            Pantalla.tocar(a, CONTINUAR, 5);
                        }),
                desvioPublicidadSim(),
                // Redirecciones temporales de Mi Claro (fines de semana): al ingresar lleva a
                // un "mundo" (p. ej. Entretenimiento) en vez del Home. Se vuelve por la pestaña.
                Desvio.si(a -> Pantalla.esta(a, PESTANA_MUNDO_CLARO) && !Pantalla.esta(a, CARGANDO),
                        "La app abrió otra sección al ingresar: se vuelve con la pestaña \"Mundo Claro\"",
                        a -> Pantalla.tocar(a, PESTANA_MUNDO_CLARO, 5)),
        };
    }

    /** La publicidad del SIM Toolkit (normalmente apagada por AdbUtils al iniciar el escenario). */
    private static Desvio desvioPublicidadSim() {
        return Desvio.si(PUBLICIDAD_SIM,
                "Se cierra la publicidad del SIM Toolkit",
                a -> Pantalla.tocarSiEsta(a, CANCELAR_PUBLICIDAD_SIM));
    }
}
