package com.hieu.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hieu.dto.RecognizeResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

@ApplicationScoped
public class PythonAiClient {

    private static final Logger log = Logger.getLogger(PythonAiClient.class);
    private static final String LINE_BREAK = "\r\n";

    @ConfigProperty(name = "python.service.url")
    String pythonServiceUrl;

    @Inject
    ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public RecognizeResponse recognizeFormulas(byte[] fileBytes, String filename, String contentType) {
        try {
            if (fileBytes == null || fileBytes.length == 0) {
                return RecognizeResponse.error("Khong nhan duoc du lieu anh tu client.");
            }

            String boundary = "----QuarkusBoundary" + UUID.randomUUID();
            byte[] multipartBody = buildMultipartBody(boundary, "image", filename, contentType, fileBytes);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(pythonServiceUrl))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(multipartBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() / 100 != 2) {
                log.errorf("Python service tra ve HTTP %d khi goi %s: %s",
                        response.statusCode(), pythonServiceUrl, response.body());
                return RecognizeResponse.error("Python service tra ve loi HTTP " + response.statusCode());
            }

            RecognizeResponse result = objectMapper.readValue(response.body(), RecognizeResponse.class);
            return result != null ? result : RecognizeResponse.error("Python service tra ve rong.");

        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.errorf(e, "Goi Python AI service that bai: %s", pythonServiceUrl);
            return RecognizeResponse.error("Khong ket noi duoc AI service (Python): " + e.getMessage());
        }
    }

    private byte[] buildMultipartBody(String boundary, String fieldName, String filename,
                                       String contentType, byte[] fileBytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        out.write(("--" + boundary + LINE_BREAK).getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + filename + "\"" + LINE_BREAK)
                .getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Type: " + contentType + LINE_BREAK).getBytes(StandardCharsets.UTF_8));
        out.write(LINE_BREAK.getBytes(StandardCharsets.UTF_8));
        out.write(fileBytes);
        out.write(LINE_BREAK.getBytes(StandardCharsets.UTF_8));
        out.write(("--" + boundary + "--" + LINE_BREAK).getBytes(StandardCharsets.UTF_8));

        return out.toByteArray();
    }
}