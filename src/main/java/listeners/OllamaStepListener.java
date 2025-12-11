package listeners;

import io.appium.java_client.AppiumDriver;
import net.serenitybdd.core.Serenity;
import net.thucydides.core.model.DataTable;
import net.thucydides.core.model.Story;
import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.steps.ExecutedStepDescription;
import net.thucydides.core.steps.StepFailure;
import net.thucydides.core.steps.StepListener;
import net.thucydides.core.webdriver.ThucydidesWebDriverSupport;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import utils.OllamaClient;
import utils.PageSourceParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Listener de Serenity optimizado para análisis con Ollama
 *
 * VERSIÓN FINAL CORREGIDA:
 * ✅ Implementa todas las firmas de StepListener correctamente
 * ✅ Maneja WebDriverFacade de Serenity
 * ✅ Solo se ejecuta cuando hay NoSuchElementException
 * ✅ Solo UNA VEZ por test
 * ✅ Captura page source correctamente
 * ✅ Puede deshabilitarse fácilmente
 *
 * Compatible con: Serenity BDD 2.0.71, Cucumber API 2.x, Java 11
 */
public class OllamaStepListener implements StepListener {

    // ✅ LEER SIN PREFIJO systemProp
    private static final boolean OLLAMA_ENABLED = Boolean.parseBoolean(
            System.getProperty("ollama.enabled", "false")
    );

    private static final String PAGE_SOURCE_DIR = "target/ollama/pagesources";
    private static final String REPORTS_DIR = "target/ollama/reports";

    private final OllamaClient ollamaClient;
    private boolean ollamaAvailable = false;
    private boolean alreadyAnalyzedCurrentTest = false;
    private String currentTestName = "";

    public OllamaStepListener() {
        System.out.println("[OLLAMA] Inicializando listener optimizado...");

        // Debug: Mostrar valor leído
        System.out.println("[OLLAMA-DEBUG] ollama.enabled = " +
                System.getProperty("ollama.enabled", "false (default)"));

        this.ollamaClient = new OllamaClient();

        // Crear directorios necesarios
        try {
            Files.createDirectories(Paths.get(PAGE_SOURCE_DIR));
            Files.createDirectories(Paths.get(REPORTS_DIR));
        } catch (IOException e) {
            System.err.println("[OLLAMA] Error al crear directorios: " + e.getMessage());
        }

        // Verificar disponibilidad de Ollama
        if (OLLAMA_ENABLED) {
            this.ollamaAvailable = checkOllamaAvailability();

            if (ollamaAvailable) {
                System.out.println("✅ [OLLAMA] Disponible y listo");
                System.out.println("   - Solo se ejecutará en fallos de localizadores");
                System.out.println("   - Máximo una vez por test");
                System.out.println("   - Para deshabilitar: ollama.enabled=false");
            } else {
                System.out.println("⚠️ [OLLAMA] No disponible");
                System.out.println("   Para habilitar: ollama run Phi3");
            }
        } else {
            System.out.println("ℹ️ [OLLAMA] Deshabilitado (ollama.enabled=false)");
        }
    }

    /**
     * ⚡ MÉTODO CLAVE: Solo se ejecuta cuando hay fallo
     * Y SOLO UNA VEZ POR TEST
     */
    @Override
    public void stepFailed(StepFailure failure) {
        if (!OLLAMA_ENABLED || !ollamaAvailable) {
            return;
        }

        if (alreadyAnalyzedCurrentTest) {
            return; // Ya se analizó este test
        }

        if (!isLocatorError(failure)) {
            return; // No es error de localizador
        }

        alreadyAnalyzedCurrentTest = true; // Marcar como analizado

        String stepName = (failure.getDescription() != null)
                ? failure.getDescription().getName()
                : "Paso desconocido";

        String errorMsg = (failure.getException() != null)
                ? failure.getException().getMessage()
                : "Sin mensaje de error";

        System.out.println("\n╔════════════════════════════════════════════════════╗");
        System.out.println("║  🔍 LOCALIZADOR NO ENCONTRADO - ANALIZANDO CON IA  ║");
        System.out.println("╚════════════════════════════════════════════════════╝");
        System.out.println("Test: " + currentTestName);
        System.out.println("Step: " + stepName);
        System.out.println("");

        analyzeLocatorFailure(stepName, errorMsg);
    }

    private boolean isLocatorError(StepFailure failure) {
        if (failure.getException() == null) {
            return false;
        }

        Throwable exception = failure.getException();
        String errorMessage = exception.getMessage() != null ? exception.getMessage().toLowerCase() : "";

        boolean isNoSuchElement = exception instanceof NoSuchElementException;
        boolean isLocatorInMessage = errorMessage.contains("unable to locate")
                || errorMessage.contains("no such element")
                || errorMessage.contains("element not found")
                || errorMessage.contains("could not find element");

        return isNoSuchElement || isLocatorInMessage;
    }

    private void analyzeLocatorFailure(String stepName, String errorMessage) {
        long startTime = System.currentTimeMillis();

        try {
            System.out.println("[OLLAMA] 1/4 Capturando page source...");

            String pageSource = capturePageSource();
            if (pageSource == null || pageSource.isEmpty()) {
                System.out.println("[OLLAMA] ⚠️  No se pudo capturar page source");
                return;
            }

            System.out.println("[OLLAMA] 2/4 Guardando XML (" + pageSource.length() + " caracteres)...");
            String xmlFileName = savePageSource(pageSource, stepName);

            System.out.println("[OLLAMA] 3/4 Extrayendo localizadores...");
            File xmlFile = new File(xmlFileName);
            List<String> availableLocators = PageSourceParser.extractRelevantLocators(xmlFile, 20); // ✅ Reducir a 20

            if (availableLocators.isEmpty()) {
                System.out.println("[OLLAMA] ⚠️  No se encontraron localizadores");
                return;
            }

            System.out.println("[OLLAMA] 4/4 Consultando a IA (" + availableLocators.size() + " localizadores)...");
            String prompt = buildLocatorFailurePrompt(stepName, errorMessage, availableLocators);

            String analysis;
            try {
                analysis = ollamaClient.ask(prompt);
            } catch (IOException e) {
                // ✅ FALLBACK: Mostrar localizadores sin análisis de IA
                System.err.println("[OLLAMA] ⚠️  Timeout - mostrando localizadores sin análisis de IA");
                analysis = generateFallbackAnalysis(availableLocators, errorMessage);
            }

            long duration = System.currentTimeMillis() - startTime;

            System.out.println("\n╔════════════════════════════════════════════════════╗");
            System.out.println("║           🤖 ANÁLISIS DE IA (OLLAMA)               ║");
            System.out.println("╚════════════════════════════════════════════════════╝");
            System.out.println(analysis);
            System.out.println("════════════════════════════════════════════════════");
            System.out.println("⏱️  Tiempo de análisis: " + (duration / 1000) + " segundos\n");

            attachAnalysisToSerenityReport(stepName, errorMessage, analysis);
            saveAnalysisReport(stepName, errorMessage, analysis);

        } catch (Exception e) {
            System.err.println("❌ [OLLAMA] Error: " + e.getMessage());
        }
    }

    /**
     * ✅ NUEVO: Generar análisis básico cuando Ollama falla
     */
    private String generateFallbackAnalysis(List<String> availableLocators, String errorMessage) {
        StringBuilder fallback = new StringBuilder();

        fallback.append("⚠️  Análisis de IA no disponible (timeout)\n\n");
        fallback.append("LOCALIZADORES DISPONIBLES EN LA PANTALLA:\n\n");

        int limit = Math.min(availableLocators.size(), 10);
        for (int i = 0; i < limit; i++) {
            fallback.append("  ").append(i + 1).append(". ").append(availableLocators.get(i)).append("\n");
        }

        fallback.append("\nSugerencia: Verifica estos localizadores manualmente\n");
        fallback.append("en el XML guardado en: target/ollama/pagesources/\n");

        return fallback.toString();
    }

    /**
     * ✅ SOLUCIÓN CRÍTICA: Desenvolver WebDriverFacade para acceder a AppiumDriver
     */
    private String capturePageSource() {
        try {
            WebDriver driver = ThucydidesWebDriverSupport.getDriver();

            if (driver == null) {
                System.out.println("[OLLAMA-DEBUG] Driver es null");
                return null;
            }

            // Desenvolver WebDriverFacade de Serenity
            WebDriver unwrappedDriver = driver;

            if (driver.getClass().getName().contains("WebDriverFacade")) {
                try {
                    java.lang.reflect.Field field = driver.getClass().getDeclaredField("proxiedWebDriver");
                    field.setAccessible(true);
                    unwrappedDriver = (WebDriver) field.get(driver);
                    System.out.println("[OLLAMA-DEBUG] Driver desenvuelto: " + unwrappedDriver.getClass().getSimpleName());
                } catch (Exception e) {
                    System.out.println("[OLLAMA-DEBUG] No se pudo desenvolver: " + e.getMessage());
                }
            }

            // Verificar si es AppiumDriver
            if (!(unwrappedDriver instanceof AppiumDriver)) {
                System.out.println("[OLLAMA-DEBUG] Driver no es AppiumDriver: " + unwrappedDriver.getClass().getSimpleName());
                return null;
            }

            String pageSource = unwrappedDriver.getPageSource();

            if (pageSource == null || pageSource.trim().isEmpty()) {
                System.out.println("[OLLAMA-DEBUG] Page source vacío");
                return null;
            }

            System.out.println("[OLLAMA-DEBUG] ✅ Page source OK: " + pageSource.length() + " caracteres");
            return pageSource;

        } catch (Exception e) {
            System.err.println("[OLLAMA-DEBUG] Error: " + e.getMessage());
            return null;
        }
    }

    private String savePageSource(String pageSource, String stepName) throws IOException {
        String sanitizedName = stepName.replaceAll("[^a-zA-Z0-9]", "_");
        String timestamp = String.valueOf(System.currentTimeMillis());
        String filename = String.format("%s/failure_%s_%s.xml", PAGE_SOURCE_DIR, sanitizedName, timestamp);

        Files.write(Paths.get(filename), pageSource.getBytes());
        System.out.println("[OLLAMA-DEBUG] XML guardado: " + filename);
        return filename;
    }

    private String buildLocatorFailurePrompt(String stepName, String errorMessage, List<String> availableLocators) {
        StringBuilder prompt = new StringBuilder();

        // ✅ PROMPT SIMPLIFICADO Y DIRECTO
        prompt.append("Analiza este error de localizador en test mobile:\n\n");

        // Extraer localizador del error
        String locatorInfo = extractLocatorFromError(errorMessage);
        prompt.append("Localizador buscado: ").append(locatorInfo).append("\n\n");

        prompt.append("Localizadores disponibles:\n");

        // ✅ REDUCIR A 20 LOCALIZADORES (antes eran 30)
        int limit = Math.min(availableLocators.size(), 20);
        for (int i = 0; i < limit; i++) {
            prompt.append((i + 1)).append(". ").append(availableLocators.get(i)).append("\n");
        }

        // ✅ PROMPT MÁS CORTO Y DIRECTO
        prompt.append("\nResponde en máximo 5 líneas:\n");
        prompt.append("1. Localizador que faltó\n");
        prompt.append("2. 2 alternativas específicas de la lista\n");
        prompt.append("3. Causa probable\n");

        return prompt.toString();
    }

    private String extractLocatorFromError(String errorMessage) {
        // Extraer el localizador del mensaje de error
        if (errorMessage.contains("Element info:")) {
            int start = errorMessage.indexOf("Element info:");
            int end = errorMessage.indexOf("\n", start);
            if (end == -1) end = errorMessage.length();
            return errorMessage.substring(start, Math.min(end, start + 200));
        }
        return "No especificado";
    }

    private void attachAnalysisToSerenityReport(String stepName, String errorMessage, String analysis) {
        StringBuilder report = new StringBuilder();
        report.append("## 🤖 Análisis de Localizador con IA\n\n");
        report.append("**Step**: ").append(stepName).append("\n\n");
        report.append("### Análisis:\n```\n");
        report.append(analysis);
        report.append("\n```\n");

        Serenity.recordReportData()
                .withTitle("🔍 Análisis Ollama")
                .andContents(report.toString());
    }

    private void saveAnalysisReport(String stepName, String errorMessage, String analysis) {
        try {
            String sanitizedName = stepName.replaceAll("[^a-zA-Z0-9]", "_");
            String timestamp = String.valueOf(System.currentTimeMillis());
            String filename = String.format("%s/analysis_%s_%s.md", REPORTS_DIR, sanitizedName, timestamp);

            StringBuilder report = new StringBuilder();
            report.append("# Análisis de Fallo\n\n");
            report.append("**Fecha**: ").append(new java.util.Date()).append("\n");
            report.append("**Step**: ").append(stepName).append("\n\n");
            report.append("## Análisis:\n\n").append(analysis).append("\n");

            Files.write(Paths.get(filename), report.toString().getBytes());
            System.out.println("[OLLAMA] 📄 Reporte: " + filename);

        } catch (IOException e) {
            System.err.println("[OLLAMA] Error al guardar reporte");
        }
    }

    private boolean checkOllamaAvailability() {
        try {
            String response = ollamaClient.ask("test");
            return response != null && !response.isEmpty();
        } catch (IOException e) {
            return false;
        }
    }

    // ===================================================================
    // Implementación de StepListener (Serenity 2.0.71)
    // Incluye AMBAS firmas de testStarted que requiere la interfaz
    // ===================================================================

    @Override
    public void testSuiteStarted(Class<?> storyClass) {}

    @Override
    public void testSuiteStarted(Story story) {}

    @Override
    public void testSuiteFinished() {}

    // ✅ FIRMA CON DOS PARÁMETROS
    @Override
    public void testStarted(String description, String id) {
        currentTestName = description;
        alreadyAnalyzedCurrentTest = false;
    }

    // ✅ FIRMA CON UN PARÁMETRO (requerida también)
    @Override
    public void testStarted(String description) {
        testStarted(description, "");
    }

    @Override
    public void testFinished(TestOutcome result) {}

    @Override
    public void testRetried() {}

    @Override
    public void stepStarted(ExecutedStepDescription description) {}

    @Override
    public void skippedStepStarted(ExecutedStepDescription description) {}

    @Override
    public void stepFinished() {}

    @Override
    public void stepIgnored() {}

    @Override
    public void stepPending() {}

    @Override
    public void stepPending(String message) {}

    @Override
    public void lastStepFailed(StepFailure stepFailure) {}

    @Override
    public void testFailed(TestOutcome testOutcome, Throwable cause) {}

    @Override
    public void testIgnored() {}

    @Override
    public void testSkipped() {}

    @Override
    public void testPending() {}

    @Override
    public void testIsManual() {}

    @Override
    public void notifyScreenChange() {}

    @Override
    public void useExamplesFrom(DataTable table) {}

    @Override
    public void addNewExamplesFrom(DataTable table) {}

    @Override
    public void exampleStarted(Map<String, String> data) {}

    @Override
    public void exampleFinished() {}

    @Override
    public void assumptionViolated(String message) {}

    @Override
    public void testRunFinished() {}
}