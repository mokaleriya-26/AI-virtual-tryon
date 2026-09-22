package com.virtualfit.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class HuggingFaceService {
    private static final Logger log = LoggerFactory.getLogger(HuggingFaceService.class);

    @Value("${huggingface.api.token:}")
    private String apiToken;

    @Value("${huggingface.api.url}")
    private String apiUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateTryOn(MultipartFile personImage, MultipartFile garmentImage, String description) throws Exception {
        log.info("IDM-VTON API schema loaded");
        log.info("Endpoint: /tryon");
        log.info("Number of inputs: 7");
        
        String baseUrl = apiUrl.contains("/api/predict") ? apiUrl.replace("/api/predict", "") : "https://yisol-idm-vton.hf.space";
        
        RestClient.Builder builder = RestClient.builder().baseUrl(baseUrl);
        if (apiToken != null && !apiToken.isEmpty()) {
            builder.defaultHeader("Authorization", "Bearer " + apiToken);
        }
        RestClient restClient = builder.build();

        try {
            // STEP 1: Upload images
            log.info("Uploading person image...");
            String personUploadedPath = uploadFileToGradio(restClient, personImage);
            log.info("Person upload successful");

            log.info("Uploading garment image...");
            String garmentUploadedPath = uploadFileToGradio(restClient, garmentImage);
            log.info("Garment upload successful");

            // STEP 2: Prepare payload
            Map<String, Object> humanDict = new HashMap<>();
            humanDict.put("background", createFileData(personUploadedPath, personImage.getOriginalFilename()));
            humanDict.put("layers", Collections.emptyList());
            humanDict.put("composite", null);

            List<Object> data = new ArrayList<>();
            data.add(humanDict);
            data.add(createFileData(garmentUploadedPath, garmentImage.getOriginalFilename()));
            data.add(description != null ? description : "Classic white button-up shirt");
            data.add(true);  // is_checked (auto-masking)
            data.add(false); // is_checked_crop
            data.add(30);    // denoise_steps
            data.add(42);    // seed

            Map<String, Object> payload = new HashMap<>();
            payload.put("data", data);

            // STEP 3: Call tryon
            log.info("Calling /call/tryon...");
            Map response = restClient.post()
                    .uri("/call/tryon")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);
            
            if (response == null || !response.containsKey("event_id")) {
                throw new RuntimeException("No event_id returned from Gradio API");
            }
            
            String eventId = (String) response.get("event_id");
            log.info("Event ID: {}", eventId);

            // STEP 4: Poll for result
            log.info("Waiting for IDM-VTON...");
            return pollForCompletedImage(baseUrl, eventId);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("[VTO ERROR] HTTP status: {}", e.getStatusCode());
            log.error("[VTO ERROR] Response body: {}", e.getResponseBodyAsString());
            log.error("[VTO ERROR] Endpoint: /call/tryon");
            log.error("[VTO ERROR] Request schema: 7 inputs");
            throw new RuntimeException("Hugging Face inference failed", e);
        } catch (Exception e) {
            log.error("[VTO ERROR] Inference failed: {}", e.getMessage(), e);
            throw new RuntimeException("Hugging Face inference failed", e);
        }
    }

    private String uploadFileToGradio(RestClient restClient, MultipartFile file) throws Exception {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("files", file.getResource());

        List<String> response = restClient.post()
                .uri("/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(List.class);

        if (response != null && !response.isEmpty()) {
            return response.get(0);
        }
        throw new RuntimeException("Failed to upload file to Gradio");
    }

    private Map<String, Object> createFileData(String path, String origName) {
        Map<String, Object> fileData = new HashMap<>();
        fileData.put("path", path);
        fileData.put("orig_name", origName);
        Map<String, String> meta = new HashMap<>();
        meta.put("_type", "gradio.FileData");
        fileData.put("meta", meta);
        return fileData;
    }

    private String pollForCompletedImage(String baseUrl, String eventId) throws Exception {
        URL url = new URL(baseUrl + "/call/tryon/" + eventId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(30_000);
        conn.setReadTimeout(30_000);
        if (apiToken != null && !apiToken.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + apiToken);
        }

        long startTime = System.currentTimeMillis();
        long timeoutMs = 5 * 60 * 1000; // 5 minutes timeout
        int responseCode = conn.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            String responseBody = readResponseBody(conn.getErrorStream());
            log.error("[VTO ERROR] Gradio polling HTTP status: {}", responseCode);
            log.error("[VTO ERROR] Gradio polling response body: {}", sanitizeForLog(responseBody));
            throw new RuntimeException("Gradio polling failed with HTTP " + responseCode + ": " + sanitizeForLog(responseBody));
        }

        try (InputStream inputStream = conn.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            String currentEvent = null;
            StringBuilder eventData = new StringBuilder();
            
            while ((line = reader.readLine()) != null) {
                if (System.currentTimeMillis() - startTime > timeoutMs) {
                    throw new RuntimeException("Timeout waiting for Gradio inference");
                }

                if (line.isEmpty()) {
                    String resultUrl = processGradioEvent(baseUrl, currentEvent, eventData.toString());
                    if (resultUrl != null) {
                        return resultUrl;
                    }
                    currentEvent = null;
                    eventData.setLength(0);
                } else if (line.startsWith(":") ) {
                    log.debug("[VTO] Gradio heartbeat received");
                } else if (line.startsWith("event:")) {
                    currentEvent = line.substring("event:".length()).trim();
                } else if (line.startsWith("data:")) {
                    if (eventData.length() > 0) {
                        eventData.append('\n');
                    }
                    eventData.append(line.substring("data:".length()).trim());
                }
            }

            String resultUrl = processGradioEvent(baseUrl, currentEvent, eventData.toString());
            if (resultUrl != null) {
                return resultUrl;
            }
        }

        throw new RuntimeException("Gradio stream ended without completion");
    }

    private String processGradioEvent(String baseUrl, String eventType, String data) throws Exception {
        if ((eventType == null || eventType.isBlank()) && (data == null || data.isBlank())) {
            return null;
        }

        String normalizedEvent = eventType == null ? "message" : eventType.trim().toLowerCase(Locale.ROOT);
        String eventBody = data == null ? "" : data.trim();
        log.info("[VTO] Queue event received");
        log.info("[VTO] Gradio event type: {}", normalizedEvent);
        log.info("[VTO] Gradio event data: {}", sanitizeForLog(eventBody));

        if ("error".equals(normalizedEvent)) {
            String details = extractErrorDetails(eventBody);
            if (eventBody.isBlank() || "null".equalsIgnoreCase(eventBody)) {
                log.error("[VTO ERROR] Gradio returned an error event with no error details.");
                log.error("[VTO ERROR] Event type: {}", normalizedEvent);
                log.error("[VTO ERROR] Raw response: {}", sanitizeForLog(eventBody));
                return null;
            }
            log.error("[VTO ERROR] Gradio stream error: {}", details);
            throw new RuntimeException("Gradio stream returned an error: " + details);
        }

        if (eventBody.isBlank()) {
            log.info("[VTO] Job status: heartbeat/empty event; continuing");
            return null;
        }

        JsonNode eventNode;
        try {
            eventNode = objectMapper.readTree(eventBody);
        } catch (Exception parseException) {
            log.warn("[VTO] Ignoring non-JSON queue event: {}", sanitizeForLog(eventBody));
            return null;
        }

        String message = textValue(eventNode, "msg");
        boolean failed = Boolean.FALSE.equals(booleanValue(eventNode, "success"))
                && ("process_completed".equalsIgnoreCase(message) || normalizedEvent.contains("complete"));
        if (failed) {
            String details = extractErrorDetails(eventBody);
            log.error("[VTO ERROR] Gradio completion failed: {}", details);
            throw new RuntimeException("Gradio inference failed: " + details);
        }

        boolean completed = Set.of("complete", "completed", "success", "process_completed").contains(normalizedEvent)
                || "process_completed".equalsIgnoreCase(message)
                || Boolean.FALSE.equals(booleanValue(eventNode, "is_generating")) && eventNode.has("output");
        if (!completed) {
            log.info("[VTO] Job status: {}{}; continuing", normalizedEvent,
                    message == null ? "" : " (" + message + ")");
            return null;
        }

        log.info("[VTO] Result event received");
        JsonNode outputNode = eventNode.has("output") ? eventNode.get("output") : eventNode;
        JsonNode dataNode = outputNode.has("data") ? outputNode.get("data") : outputNode;
        String outputType = dataNode == null || dataNode.isNull() ? "null" : dataNode.getNodeType().toString();
        log.info("[VTO] Output type: {}", outputType);

        String outputUrl = extractImageUrl(dataNode, baseUrl);
        if (outputUrl == null) {
            throw new RuntimeException("Completed Gradio event did not contain an image file reference: "
                    + sanitizeForLog(eventBody));
        }

        log.info("[VTO] Output file URL: {}", sanitizeForLog(outputUrl));
        validateGeneratedImage(outputUrl);
        return outputUrl;
    }

    private String extractImageUrl(JsonNode node, String baseUrl) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isArray()) {
            for (JsonNode item : node) {
                String imageUrl = extractImageUrl(item, baseUrl);
                if (imageUrl != null) {
                    return imageUrl;
                }
            }
            return null;
        }
        if (node.isTextual() && !node.asText().isBlank()) {
            return resolveGradioFileReference(node.asText(), baseUrl);
        }
        if (!node.isObject()) {
            return null;
        }

        for (String fieldName : List.of("url", "path", "name")) {
            String value = textValue(node, fieldName);
            if (value != null && !value.isBlank()) {
                return resolveGradioFileReference(value, baseUrl);
            }
        }
        for (JsonNode child : node) {
            String imageUrl = extractImageUrl(child, baseUrl);
            if (imageUrl != null) {
                return imageUrl;
            }
        }
        return null;
    }

    private String resolveGradioFileReference(String reference, String baseUrl) {
        if (reference.startsWith("http://") || reference.startsWith("https://")) {
            return reference;
        }
        if (reference.startsWith("/file=")) {
            return baseUrl + reference;
        }
        if (reference.startsWith("/")) {
            return baseUrl + "/file=" + reference;
        }
        return baseUrl + "/file=/" + reference;
    }

    private void validateGeneratedImage(String imageUrl) throws Exception {
        log.info("[VTO] Downloading generated image...");
        HttpURLConnection imageConnection = (HttpURLConnection) new URL(imageUrl).openConnection();
        imageConnection.setRequestMethod("GET");
        imageConnection.setConnectTimeout(30_000);
        imageConnection.setReadTimeout(60_000);
        if (apiToken != null && !apiToken.isEmpty()) {
            imageConnection.setRequestProperty("Authorization", "Bearer " + apiToken);
        }

        int responseCode = imageConnection.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            String responseBody = readResponseBody(imageConnection.getErrorStream());
            throw new RuntimeException("Generated image download failed with HTTP " + responseCode + ": "
                    + sanitizeForLog(responseBody));
        }

        String contentType = imageConnection.getContentType();
        byte[] imageBytes;
        try (InputStream imageStream = imageConnection.getInputStream()) {
            imageBytes = imageStream.readAllBytes();
        }
        log.info("[VTO] Generated image bytes: {}, Content-Type: {}", imageBytes.length, contentType);

        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new RuntimeException("Generated result was not an image. Content-Type: " + contentType);
        }
        if (imageBytes.length == 0) {
            throw new RuntimeException("Generated image response was empty");
        }

        BufferedImage generatedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (generatedImage == null) {
            throw new RuntimeException("Generated image bytes could not be decoded");
        }
        log.info("[VTO] Generated image validated successfully: {}x{}", generatedImage.getWidth(), generatedImage.getHeight());
    }

    private String extractErrorDetails(String eventBody) {
        if (eventBody == null || eventBody.isBlank() || "null".equalsIgnoreCase(eventBody)) {
            return "Gradio returned an empty error event";
        }
        try {
            JsonNode node = objectMapper.readTree(eventBody);
            for (String fieldName : List.of("error", "message", "detail", "exception", "reason")) {
                String value = textValue(node, fieldName);
                if (value != null && !value.isBlank()) {
                    return sanitizeForLog(value);
                }
            }
        } catch (Exception ignored) {
            // Preserve the raw non-JSON error below.
        }
        return sanitizeForLog(eventBody);
    }

    private String textValue(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName) || node.get(fieldName).isNull()) {
            return null;
        }
        JsonNode value = node.get(fieldName);
        return value.isValueNode() ? value.asText() : null;
    }

    private Boolean booleanValue(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName) || !node.get(fieldName).isBoolean()) {
            return null;
        }
        return node.get(fieldName).asBoolean();
    }

    private String readResponseBody(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            return "<empty response body>";
        }
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private String sanitizeForLog(String value) {
        if (value == null || value.isBlank()) {
            return "<empty>";
        }
        String sanitized = value.replaceAll("(?i)bearer\\s+[^\\s,}]+", "Bearer [REDACTED]");
        sanitized = sanitized.replaceAll("(?i)([?&](?:token|api[_-]?key|secret|authorization)=)[^&\\s]+", "$1[REDACTED]");
        return sanitized.length() > 4000 ? sanitized.substring(0, 4000) + "..." : sanitized;
    }
}
