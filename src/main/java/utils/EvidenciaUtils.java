package utils;

import hooks.ReportHooks;
import net.serenitybdd.core.Serenity;

import java.awt.image.BufferedImage;
import java.util.LinkedHashSet;
import java.util.Set;

public class EvidenciaUtils {

    private static int contadorPasos = 1;
    private static final Set<String> evidenciasRegistradas = new LinkedHashSet<>();

    public static void registrarCaptura(String paso) {
        String pasoNumerado = contadorPasos++ + " " + paso;

        // Registrar en tu sistema actual
        ReportHooks.registrarPaso(pasoNumerado);
        CapturaDePantallaMovil.tomarCapturaPantalla(pasoNumerado);

        // Integrar con Serenity (versión simple)
        Serenity.recordReportData().withTitle(paso).andContents(pasoNumerado);
        Serenity.takeScreenshot();
    }

    /**
     * Registra una imagen ya generada (ej. la tabla de resultados) como un paso
     * más del reporte, en lugar de tomar una captura del dispositivo.
     */
    public static void registrarImagen(String paso, BufferedImage imagen) {
        String pasoNumerado = contadorPasos++ + " " + paso;

        ReportHooks.registrarPaso(pasoNumerado);
        CapturaDePantallaMovil.guardarImagen(pasoNumerado, imagen);

        Serenity.recordReportData().withTitle(paso).andContents(pasoNumerado);
    }

    /**
     * Registra una evidencia con nombre FIJO: el archivo se reescribe en cada
     * reintento (no se acumulan capturas) y el paso se registra una sola vez,
     * de modo que al final solo queda una imagen por evidencia.
     */
    public static void registrarEvidencia(String nombre) {
        // Solo la captura propia (Capturas/). Se omite Serenity.takeScreenshot() a
        // propósito: era una SEGUNDA captura del dispositivo por evidencia y en un
        // móvil real cada una cuesta varios segundos.
        CapturaDePantallaMovil.tomarCapturaPantallaFija(nombre);

        if (evidenciasRegistradas.add(nombre)) {
            ReportHooks.registrarPaso(nombre);
            Serenity.recordReportData().withTitle(nombre).andContents(nombre);
        }
    }

    public static void reiniciarContador() {
        contadorPasos = 1;
        evidenciasRegistradas.clear();
    }
}