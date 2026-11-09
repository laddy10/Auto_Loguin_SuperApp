package utils;

import hooks.ReportHooks;

import java.awt.image.BufferedImage;
import java.util.LinkedHashSet;
import java.util.Set;

public class EvidenciaUtils {

    private static int contadorPasos = 1;
    private static final Set<String> evidenciasRegistradas = new LinkedHashSet<>();

    /**
     * Registra una imagen ya generada (ej. la tabla de resultados) como un paso
     * más del reporte, en lugar de tomar una captura del dispositivo.
     *
     * Va a Capturas/ (Word y WhatsApp) y se adjunta al paso abierto del reporte.
     */
    public static void registrarImagen(String paso, BufferedImage imagen) {
        String pasoNumerado = contadorPasos++ + " " + paso;

        ReportHooks.registrarPaso(pasoNumerado);
        CapturaDePantallaMovil.guardarImagen(pasoNumerado, imagen);
        ReporteSerenity.adjuntarImagen(paso, imagen, true);
    }

    /**
     * Registra una evidencia con nombre FIJO: el archivo de Capturas/ se reescribe en
     * cada reintento (no se acumulan capturas) y el paso del Word se registra una sola
     * vez, de modo que al final solo queda una imagen por evidencia.
     *
     * La MISMA captura se adjunta al paso abierto del reporte de Serenity / Smart
     * Tester: el dispositivo se fotografía una sola vez por evidencia.
     */
    public static void registrarEvidencia(String nombre) {
        BufferedImage captura = CapturaDePantallaMovil.tomarCapturaPantallaFija(nombre);
        ReporteSerenity.adjuntarImagen(nombre, captura, false);

        if (evidenciasRegistradas.add(nombre)) {
            ReportHooks.registrarPaso(nombre);
        }
    }

    public static void reiniciarContador() {
        contadorPasos = 1;
        evidenciasRegistradas.clear();
    }
}
