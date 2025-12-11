package utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Cliente para interactuar con Ollama API
 *
 * VERSIÓN OPTIMIZADA:
 * ✅ Timeout aumentado a 3 minutos
 * ✅ Manejo robusto de errores
 * ✅ Logs informativos
 */
public class OllamaClient {

    private static final String OLLAMA_URL = System.getProperty(
            "ollama.url",
            "http://127.0.0.1:11434/api/generate"
    );

    private static final String MODEL = System.getProperty(
            "ollama.model",
            "Phi3"
    );

    // ✅ AUMENTAR TIMEOUT A 3 MINUTOS (180 segundos)
    private static final int CONNECT_TIMEOUT = 10_000;  // 10 segundos para conectar
    private static final int READ_TIMEOUT = 180_000;    // 3 minutos para leer respuesta

    private final Gson gson;

    public OllamaClient() {
        this.gson = new Gson();
        System.out.println("[OLLAMA-CLIENT] Configurado:");
        System.out.println("  URL: " + OLLAMA_URL);
        System.out.println("  Modelo: " + MODEL);
        System.out.println("  Timeout lectura: " + (READ_TIMEOUT / 1000) + " segundos");
    }

    /**
     * Envía un prompt a Ollama y obtiene la respuesta completa
     *
     * @param prompt El prompt a enviar
     * @return La respuesta de Ollama
     * @throws IOException Si hay error de comunicación
     */
    public String ask(String prompt) throws IOException {
        long startTime = System.currentTimeMillis();

        try {
            URL url = new URL(OLLAMA_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Configurar conexión
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // ✅ CONFIGURAR TIMEOUTS
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);

            // Construir request JSON
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", MODEL);
            requestBody.addProperty("prompt", prompt);
            requestBody.addProperty("stream", false);

            // Enviar request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Leer respuesta
            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Ollama API error: HTTP " + responseCode);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            // Parsear respuesta JSON
            JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
            String answer = jsonResponse.get("response").getAsString();

            long duration = System.currentTimeMillis() - startTime;
            System.out.println("[OLLAMA-CLIENT] ✅ Respuesta recibida en " + (duration / 1000) + " segundos");

            return answer;

        } catch (java.net.SocketTimeoutException e) {
            long duration = System.currentTimeMillis() - startTime;
            System.err.println("[OLLAMA-CLIENT] ❌ Timeout después de " + (duration / 1000) + " segundos");
            throw new IOException("Ollama timeout - considera usar un modelo más rápido o aumentar el timeout", e);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            System.err.println("[OLLAMA-CLIENT] ❌ Error después de " + (duration / 1000) + " segundos: " + e.getMessage());
            throw new IOException("Error comunicando con Ollama: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica si Ollama está disponible
     *
     * @return true si Ollama responde, false en caso contrario
     */
    public boolean isAvailable() {
        try {
            String response = ask("test");
            return response != null && !response.isEmpty();
        } catch (IOException e) {
            return false;
        }
    }
}