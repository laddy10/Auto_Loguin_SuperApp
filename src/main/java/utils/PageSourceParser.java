package utils;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public final class PageSourceParser {

    private PageSourceParser() {}

    /**
     * Extrae y devuelve una lista ordenada (sin duplicados) de localizadores relevantes
     * (resource-id, content-desc, text) desde el XML.
     *
     * @param xmlFile archivo XML del page source
     * @param limit máximo de localizadores a devolver (usa -1 para sin límite)
     * @return lista de strings normalizados tipo resource-id="..." o text="..."
     */
    public static List<String> extractRelevantLocators(File xmlFile, int limit) {
        if (xmlFile == null || !xmlFile.exists()) return Collections.emptyList();

        // Use LinkedHashSet para mantener orden y evitar duplicados
        Set<String> locators = new LinkedHashSet<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            // ****** Seguridad: prevenir XXE ******
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            // *************************************

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            document.getDocumentElement().normalize();

            NodeList nodes = document.getElementsByTagName("*");
            for (int i = 0; i < nodes.getLength(); i++) {
                if (!(nodes.item(i) instanceof Element)) continue;
                Element element = (Element) nodes.item(i);

                // resource-id
                if (element.hasAttribute("resource-id")) {
                    String v = element.getAttribute("resource-id").trim();
                    if (!v.isEmpty()) locators.add(normalize("resource-id", v));
                }

                // content-desc (Android)
                if (element.hasAttribute("content-desc")) {
                    String v = element.getAttribute("content-desc").trim();
                    if (!v.isEmpty()) locators.add(normalize("content-desc", v));
                }

                // text
                if (element.hasAttribute("text")) {
                    String v = element.getAttribute("text").trim();
                    if (!v.isEmpty()) locators.add(normalize("text", v));
                }

                // accessibilityLabel/name (iOS) — opcional
                if (element.hasAttribute("name")) {
                    String v = element.getAttribute("name").trim();
                    if (!v.isEmpty()) locators.add(normalize("name", v));
                }
            }
        } catch (Exception e) {
            // No interrumpir el flujo; devolver lo que haya extraído
            e.printStackTrace();
        }

        List<String> list = new ArrayList<>(locators);
        if (limit > 0 && list.size() > limit) {
            return list.subList(0, limit);
        }
        return list;
    }

    private static String normalize(String attr, String value) {
        // Limpiar espacios múltiples y acentos opcionalmente, convertir a forma segura
        String cleaned = value.replaceAll("\\s+", " ").trim();
        // Opcional: eliminar valores vacíos o placeholders (ej: "null", "N/A")
        if (cleaned.equalsIgnoreCase("null") || cleaned.equalsIgnoreCase("n/a")) {
            return "";
        }
        return attr + "=\"" + escapeForPrompt(cleaned) + "\"";
    }

    private static String escapeForPrompt(String s) {
        // Minimiza problemas con comillas en el prompt
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }
}
