package tasks.Ollama;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;
import utils.OllamaClient;
import utils.PageSourceParser;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static net.serenitybdd.core.Serenity.recordReportData;

public class ValidateLocatorsWithOllama implements Task {

    private final String xmlFileName;
    private final String locatorsOrTexts; // opcional: localizadores específicos pasados desde feature
    private final int maxLocatorsToSend;  // configurable

    public ValidateLocatorsWithOllama(String xmlFileName, String locatorsOrTexts, int maxLocatorsToSend) {
        this.xmlFileName = xmlFileName;
        this.locatorsOrTexts = locatorsOrTexts;
        this.maxLocatorsToSend = maxLocatorsToSend;
    }

    public static ValidateLocatorsWithOllama using(String xmlFileName, String locatorsOrTexts, int maxLocatorsToSend) {
        return Tasks.instrumented(ValidateLocatorsWithOllama.class, xmlFileName, locatorsOrTexts, maxLocatorsToSend);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        try {
            File xmlFile = new File("src/test/resources/pagesource/" + xmlFileName);
            List<String> extracted = PageSourceParser.extractRelevantLocators(xmlFile, maxLocatorsToSend);

            // Si additionally se pasan localizadores concretos en locatorsOrTexts los añadimos (evitando duplicados)
            if (locatorsOrTexts != null && !locatorsOrTexts.trim().isEmpty()) {
                List<String> extras = List.of(locatorsOrTexts.split(","))
                        .stream()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(s -> {
                            // si ya vienen en formato attr="value" lo dejamos, si no asumimos id/text
                            if (s.contains("=") || s.contains("\"")) return s;
                            return "text=\"" + s.replace("\"", "\\\"") + "\"";
                        })
                        .collect(Collectors.toList());
                // agregarlos al inicio para priorizarlos
                for (String e : extras) {
                    if (!extracted.contains(e)) extracted.add(0, e);
                }
            }

            // Si no hay nada para enviar
            if (extracted.isEmpty()) {
                recordReportData().withTitle("Validación de localizadores (Ollama)")
                        .andContents("No se extrajeron localizadores del XML: " + xmlFileName);
                return;
            }

            // Construir prompt resumido
            StringBuilder prompt = new StringBuilder();
            prompt.append("Estoy validando localizadores de UI para una pantalla. ");
            prompt.append("Estos son los localizadores (atributo=\"valor\") extraídos del page source:\n\n");
            for (String loc : extracted) {
                prompt.append("- ").append(loc).append("\n");
            }
            prompt.append("\nPor favor indica, para cada localizador, si existe en el XML y si no, sugiere un localizador alternativo (resource-id, content-desc o xpath breve). Respóndeme en formato de lista.");

            // Llamada a Ollama
            OllamaClient ollama = new OllamaClient();
            String response = ollama.ask(prompt.toString());

            // Guardar respuesta limpia en report
            recordReportData()
                    .withTitle("Validación de Localizadores contra " + xmlFileName)
                    .andContents(response);

        } catch (Exception e) {
            recordReportData()
                    .withTitle("Error Validación Localizadores")
                    .andContents("No se pudo validar UI automáticamente: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}

