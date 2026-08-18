package utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Genera una imagen PNG con la tabla de resultados del login completo, con un
 * estilo similar a la plantilla de monitoreo (encabezado azul, primera columna
 * azul claro y estatus en verde/rojo). Usa AWT/Graphics2D, sin dependencias.
 */
public class TablaResultados {

    /** Una fila de la tabla. */
    public static class Fila {
        public final String metodo;
        public final boolean exitoso;
        public final String hora;
        public final String so;

        public Fila(String metodo, boolean exitoso, String hora, String so) {
            this.metodo = metodo;
            this.exitoso = exitoso;
            this.hora = hora;
            this.so = so;
        }
    }

    private static final int PADDING = 20;
    private static final int ROW_HEIGHT = 50;

    private static final int COL_METODO = 300;
    private static final int COL_ESTATUS = 150;
    private static final int COL_HORA = 110;
    private static final int COL_SO = 140;

    // Colores tipo Excel del ejemplo
    private static final Color AZUL_HEADER = new Color(91, 155, 213);
    private static final Color AZUL_CLARO = new Color(189, 215, 238);
    private static final Color VERDE_OK = new Color(0, 176, 80);
    private static final Color ROJO_FALLO = new Color(214, 69, 65);
    private static final Color NEGRO = new Color(20, 20, 20);
    private static final Color BORDE = new Color(60, 60, 60);

    public static BufferedImage crearImagen(List<Fila> filas) {
        int tablaAncho = COL_METODO + COL_ESTATUS + COL_HORA + COL_SO;
        int ancho = tablaAncho + PADDING * 2;
        int alto = PADDING * 2 + ROW_HEIGHT * (filas.size() + 1);

        BufferedImage img = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, ancho, alto);

        int x0 = PADDING;
        int y = PADDING;

        int[] colX = {
                x0,
                x0 + COL_METODO,
                x0 + COL_METODO + COL_ESTATUS,
                x0 + COL_METODO + COL_ESTATUS + COL_HORA
        };
        int[] colW = {COL_METODO, COL_ESTATUS, COL_HORA, COL_SO};
        String[] headers = {"Monitoreo Inicio de sesion", "Estatus", "Hora", "SO"};

        // Encabezado (azul)
        g.setColor(AZUL_HEADER);
        g.fillRect(x0, y, tablaAncho, ROW_HEIGHT);
        g.setColor(NEGRO);
        g.setFont(new Font("SansSerif", Font.BOLD, 17));
        for (int c = 0; c < headers.length; c++) {
            dibujarTextoCelda(g, headers[c], colX[c], y, colW[c], ROW_HEIGHT, true);
        }
        y += ROW_HEIGHT;

        // Filas de datos
        for (Fila f : filas) {
            // Primera columna (azul claro)
            g.setColor(AZUL_CLARO);
            g.fillRect(colX[0], y, colW[0], ROW_HEIGHT);
            // Estatus (verde/rojo)
            g.setColor(f.exitoso ? VERDE_OK : ROJO_FALLO);
            g.fillRect(colX[1], y, colW[1], ROW_HEIGHT);
            // Hora y SO (blanco)
            g.setColor(Color.WHITE);
            g.fillRect(colX[2], y, colW[2] + colW[3], ROW_HEIGHT);

            // Texto método (alineado a la izquierda)
            g.setColor(NEGRO);
            g.setFont(new Font("SansSerif", Font.BOLD, 15));
            dibujarTextoCelda(g, f.metodo, colX[0], y, colW[0], ROW_HEIGHT, false);

            // Texto estatus
            g.setColor(f.exitoso ? NEGRO : Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 16));
            dibujarTextoCelda(g, f.exitoso ? "OK" : "FALLO", colX[1], y, colW[1], ROW_HEIGHT, true);

            // Hora y SO
            g.setColor(NEGRO);
            g.setFont(new Font("SansSerif", Font.BOLD, 15));
            dibujarTextoCelda(g, f.hora, colX[2], y, colW[2], ROW_HEIGHT, true);
            dibujarTextoCelda(g, f.so, colX[3], y, colW[3], ROW_HEIGHT, true);

            y += ROW_HEIGHT;
        }

        // Bordes
        g.setColor(BORDE);
        g.setStroke(new BasicStroke(1.2f));
        int top = PADDING;
        int bottom = y;
        for (int gy = top; gy <= bottom; gy += ROW_HEIGHT) {
            g.drawLine(x0, gy, x0 + tablaAncho, gy);
        }
        int vx = x0;
        g.drawLine(vx, top, vx, bottom);
        for (int w : colW) {
            vx += w;
            g.drawLine(vx, top, vx, bottom);
        }

        g.dispose();
        return img;
    }

    private static void dibujarTextoCelda(Graphics2D g, String texto, int x, int y, int w, int h, boolean centrado) {
        FontMetrics fm = g.getFontMetrics();
        int ty = y + (h - fm.getHeight()) / 2 + fm.getAscent();
        int tx = centrado ? x + (w - fm.stringWidth(texto)) / 2 : x + 10;
        g.drawString(texto, tx, ty);
    }
}
