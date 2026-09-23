package com.virtualfit.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class HuggingFaceService {

    private static final Logger log =
            LoggerFactory.getLogger(HuggingFaceService.class);

    @Value("${huggingface.api.token:}")
    private String apiToken;

    @Value("${huggingface.api.url}")
    private String apiUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateTryOn(
            MultipartFile personImage,
            MultipartFile garmentImage,
            String description
    ) throws Exception {

        log.info("========================================");
        log.info("[VTO] Starting IDM-VTON inference");
        log.info("========================================");

        String baseUrl = getBaseUrl();

        log.info("[VTO] Base URL: {}", baseUrl);
        log.info("[VTO] Endpoint: /call/tryon");
        log.info("[VTO] Number of inputs: 7");

        RestClient.Builder builder =
                RestClient.builder().baseUrl(baseUrl);

        if (apiToken != null && !apiToken.isBlank()) {
            builder.defaultHeader(
                    "Authorization",
                    "Bearer " + apiToken
            );
        }

        RestClient restClient = builder.build();

        try {

            // =====================================================
            // STEP 1 — Upload person image
            // =====================================================

            log.info("[VTO] Uploading person image...");

            String personUploadedPath =
                    uploadFileToGradio(
                            restClient,
                            personImage
                    );

            log.info(
                    "[VTO] Person upload successful: {}",
                    sanitizeForLog(personUploadedPath)
            );


            // =====================================================
            // STEP 2 — Upload garment image
            // =====================================================

            log.info("[VTO] Uploading garment image...");

            String garmentUploadedPath =
                    uploadFileToGradio(
                            restClient,
                            garmentImage
                    );

            log.info(
                    "[VTO] Garment upload successful: {}",
                    sanitizeForLog(garmentUploadedPath)
            );


            // =====================================================
            // STEP 3 — Prepare IDM-VTON payload
            // =====================================================

            Map<String, Object> humanDict =
                    new HashMap<>();

            humanDict.put(
                    "background",
                    createFileData(
                            personUploadedPath,
                            personImage.getOriginalFilename()
                    )
            );

            humanDict.put(
                    "layers",
                    Collections.emptyList()
            );

            humanDict.put(
                    "composite",
                    null
            );


            List<Object> data =
                    new ArrayList<>();

            data.add(humanDict);

            data.add(
                    createFileData(
                            garmentUploadedPath,
                            garmentImage.getOriginalFilename()
                    )
            );

            data.add(
                    description != null && !description.isBlank()
                            ? description
                            : "Classic white button-up shirt"
            );

            // is_checked
            data.add(true);

            // is_checked_crop
            data.add(false);

            // denoise_steps
            data.add(30);

            // seed
            data.add(42);


            Map<String, Object> payload =
                    new HashMap<>();

            payload.put("data", data);


            log.info(
                    "[VTO] Payload prepared with {} inputs",
                    data.size()
            );


            // =====================================================
            // STEP 4 — Start Gradio job
            // =====================================================

            log.info("[VTO] Calling /call/tryon...");

            Map<?, ?> response =
                    restClient.post()
                            .uri("/call/tryon")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(payload)
                            .retrieve()
                            .body(Map.class);


            if (response == null) {
                throw new RuntimeException(
                        "Gradio returned an empty response when starting the job"
                );
            }


            log.info(
                    "[VTO] Start response: {}",
                    sanitizeForLog(
                            objectMapper.writeValueAsString(response)
                    )
            );


            Object eventIdObject =
                    response.get("event_id");

            if (eventIdObject == null) {
                throw new RuntimeException(
                        "No event_id returned from Gradio API: "
                                + sanitizeForLog(
                                objectMapper.writeValueAsString(response)
                        )
                );
            }


            String eventId =
                    String.valueOf(eventIdObject);


            log.info(
                    "[VTO] Event ID: {}",
                    eventId
            );


            // =====================================================
            // STEP 5 — Wait for generated image
            // =====================================================

            log.info(
                    "[VTO] Waiting for IDM-VTON inference..."
            );

            String resultUrl =
                    pollForCompletedImage(
                            baseUrl,
                            eventId
                    );


            log.info(
                    "[VTO] IDM-VTON inference completed successfully"
            );

            log.info(
                    "[VTO] Result URL: {}",
                    sanitizeForLog(resultUrl)
            );

            log.info("========================================");


            return resultUrl;


        } catch (
                HttpClientErrorException |
                HttpServerErrorException e
        ) {

            log.error(
                    "[VTO ERROR] HTTP status: {}",
                    e.getStatusCode()
            );

            log.error(
                    "[VTO ERROR] Response body: {}",
                    sanitizeForLog(
                            e.getResponseBodyAsString()
                    )
            );

            throw new RuntimeException(
                    "Hugging Face inference failed: HTTP "
                            + e.getStatusCode()
                            + " from /call/tryon",
                    e
            );


        } catch (Exception e) {

            log.error(
                    "[VTO ERROR] Inference failed: {}",
                    e.getMessage(),
                    e
            );

            throw new RuntimeException(
                    "Hugging Face inference failed: "
                            + (
                            e.getMessage() == null
                                    ? "unknown Gradio error"
                                    : e.getMessage()
                    ),
                    e
            );
        }
    }


    // =============================================================
    // BASE URL
    // =============================================================

    private String getBaseUrl() {

        if (apiUrl == null || apiUrl.isBlank()) {

            return "https://yisol-idm-vton.hf.space";
        }

        if (apiUrl.contains("/api/predict")) {

            return apiUrl.replace(
                    "/api/predict",
                    ""
            );
        }

        if (apiUrl.endsWith("/")) {

            return apiUrl.substring(
                    0,
                    apiUrl.length() - 1
            );
        }

        return apiUrl;
    }


    // =============================================================
    // FILE UPLOAD
    // =============================================================

    private String uploadFileToGradio(
            RestClient restClient,
            MultipartFile file
    ) throws Exception {

        if (file == null || file.isEmpty()) {

            throw new RuntimeException(
                    "Cannot upload an empty file"
            );
        }


        MultiValueMap<String, Object> body =
                new LinkedMultiValueMap<>();

        body.add(
                "files",
                file.getResource()
        );


        List<?> response =
                restClient.post()
                        .uri("/upload")
                        .contentType(
                                MediaType.MULTIPART_FORM_DATA
                        )
                        .body(body)
                        .retrieve()
                        .body(List.class);


        if (response != null &&
                !response.isEmpty() &&
                response.get(0) != null) {

            return String.valueOf(
                    response.get(0)
            );
        }


        throw new RuntimeException(
                "Failed to upload file to Gradio"
        );
    }


    // =============================================================
    // GRADIO FILE DATA
    // =============================================================

    private Map<String, Object> createFileData(
            String path,
            String originalName
    ) {

        Map<String, Object> fileData =
                new LinkedHashMap<>();

        fileData.put(
                "path",
                path
        );

        fileData.put(
                "orig_name",
                originalName != null
                        ? originalName
                        : "image.jpg"
        );


        Map<String, String> meta =
                new HashMap<>();

        meta.put(
                "_type",
                "gradio.FileData"
        );

        fileData.put(
                "meta",
                meta
        );


        return fileData;
    }


    // =============================================================
    // SSE POLLING
    // =============================================================

    private String pollForCompletedImage(
            String baseUrl,
            String eventId
    ) throws Exception {

        String endpoint =
                baseUrl
                        + "/call/tryon/"
                        + eventId;


        log.info(
                "[VTO] Opening Gradio SSE stream: {}",
                endpoint
        );


        HttpURLConnection connection =
                (HttpURLConnection)
                        new URL(endpoint).openConnection();


        connection.setRequestMethod("GET");

        connection.setConnectTimeout(
                30_000
        );

        connection.setReadTimeout(
                30_000
        );

        connection.setRequestProperty(
                "Accept",
                "text/event-stream"
        );

        connection.setRequestProperty(
                "Cache-Control",
                "no-cache"
        );

        connection.setRequestProperty(
                "Connection",
                "keep-alive"
        );


        if (apiToken != null &&
                !apiToken.isBlank()) {

            connection.setRequestProperty(
                    "Authorization",
                    "Bearer " + apiToken
            );
        }


        int responseCode =
                connection.getResponseCode();


        log.info(
                "[VTO] SSE HTTP status: {}",
                responseCode
        );


        if (responseCode < 200 ||
                responseCode >= 300) {

            String responseBody =
                    readResponseBody(
                            connection.getErrorStream()
                    );

            log.error(
                    "[VTO ERROR] Gradio SSE HTTP status: {}",
                    responseCode
            );

            log.error(
                    "[VTO ERROR] Gradio SSE response: {}",
                    sanitizeForLog(responseBody)
            );


            throw new RuntimeException(
                    "Gradio polling failed with HTTP "
                            + responseCode
                            + ": "
                            + sanitizeForLog(
                            responseBody
                    )
            );
        }


        long startTime =
                System.currentTimeMillis();

        long timeoutMs =
                5 * 60 * 1000L;


        String currentEvent = null;

        StringBuilder eventData =
                new StringBuilder();


        try (
                InputStream inputStream =
                        connection.getInputStream();

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        inputStream,
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {


            String line;


            while ((line = reader.readLine()) != null) {

                if (
                        System.currentTimeMillis()
                                - startTime
                                > timeoutMs
                ) {

                    throw new RuntimeException(
                            "Timeout waiting for Gradio inference"
                    );
                }


                /*
                 * readLine() removes the newline but may
                 * leave the event structure dependent on
                 * how the server sends the SSE message.
                 *
                 * Treat whitespace-only lines as event
                 * boundaries.
                 */

                if (line.trim().isEmpty()) {

                    String result =
                            processSseEvent(
                                    baseUrl,
                                    currentEvent,
                                    eventData.toString()
                            );


                    if (result != null) {

                        return result;
                    }


                    currentEvent = null;

                    eventData.setLength(0);

                    continue;
                }


                if (line.startsWith(":")) {

                    log.debug(
                            "[VTO] Gradio heartbeat/comment received"
                    );

                    continue;
                }


                if (line.startsWith("event:")) {

                    /*
                     * If a previous event has not been
                     * terminated by a blank line, process
                     * it before starting the new event.
                     */

                    if (
                            currentEvent != null &&
                            eventData.length() > 0
                    ) {

                        String result =
                                processSseEvent(
                                        baseUrl,
                                        currentEvent,
                                        eventData.toString()
                                );


                        if (result != null) {

                            return result;
                        }


                        eventData.setLength(0);
                    }


                    currentEvent =
                            line.substring(
                                    "event:".length()
                            ).trim();


                    log.debug(
                            "[VTO] SSE event: {}",
                            currentEvent
                    );


                    continue;
                }


                if (line.startsWith("data:")) {

                    String value =
                            line.substring(
                                    "data:".length()
                            ).trim();


                    if (eventData.length() > 0) {

                        eventData.append('\n');
                    }


                    eventData.append(value);

                    continue;
                }


                /*
                 * Ignore unknown SSE fields.
                 */

                log.debug(
                        "[VTO] Ignoring SSE line: {}",
                        sanitizeForLog(line)
                );
            }


            /*
             * IMPORTANT:
             *
             * Gradio may close the SSE connection after
             * sending the final event. Therefore we MUST
             * process the buffered event at EOF.
             */

            if (
                    currentEvent != null ||
                    eventData.length() > 0
            ) {

                log.info(
                        "[VTO] Processing final buffered SSE event"
                );


                String result =
                        processSseEvent(
                                baseUrl,
                                currentEvent,
                                eventData.toString()
                        );


                if (result != null) {

                    return result;
                }
            }
        }


        /*
         * The stream really ended without a completion
         * or error event.
         */

        throw new RuntimeException(
                "Gradio stream ended without completion. "
                        + "No 'complete' event containing a result was received."
        );
    }


    // =============================================================
    // PROCESS ONE SSE EVENT
    // =============================================================

    private String processSseEvent(
            String baseUrl,
            String eventType,
            String data
    ) throws Exception {

        if (
                (eventType == null ||
                        eventType.isBlank())
                        &&
                        (data == null ||
                                data.isBlank())
        ) {

            return null;
        }


        String normalizedEvent =
                eventType == null
                        ? "message"
                        : eventType
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );


        String eventBody =
                data == null
                        ? ""
                        : data.trim();


        log.info(
                "[VTO] Gradio event: {}",
                normalizedEvent
        );

        log.info(
                "[VTO] Gradio data: {}",
                sanitizeForLog(eventBody)
        );


        // =========================================================
        // ERROR EVENT
        // =========================================================

        if ("error".equals(normalizedEvent)) {

            String details =
                    extractErrorDetails(
                            eventBody
                    );


            log.error(
                    "[VTO ERROR] Gradio error event: {}",
                    details
            );


            throw new RuntimeException(
                    "Gradio stream returned an error: "
                            + details
            );
        }


        // =========================================================
        // HEARTBEAT
        // =========================================================

        if ("heartbeat".equals(normalizedEvent)) {

            log.debug(
                    "[VTO] Gradio heartbeat"
            );

            return null;
        }


        // =========================================================
        // EMPTY DATA
        // =========================================================

        if (eventBody.isBlank() ||
                "null".equalsIgnoreCase(eventBody)) {

            log.info(
                    "[VTO] Empty Gradio event data; continuing"
            );

            return null;
        }


        JsonNode node;


        try {

            node =
                    objectMapper.readTree(
                            eventBody
                    );

        } catch (Exception e) {

            log.warn(
                    "[VTO] Non-JSON Gradio event ignored: {}",
                    sanitizeForLog(eventBody)
            );

            return null;
        }


        // =========================================================
        // COMPLETE EVENT
        // =========================================================

        if ("complete".equals(normalizedEvent) ||
                "completed".equals(normalizedEvent) ||
                "success".equals(normalizedEvent)) {

            log.info(
                    "[VTO] Final Gradio completion event received"
            );


            String imageUrl =
                    extractImageUrl(
                            node,
                            baseUrl
                    );


            if (imageUrl == null) {

                throw new RuntimeException(
                        "Gradio reported completion but no image "
                                + "file was found in the response: "
                                + sanitizeForLog(eventBody)
                );
            }


            log.info(
                    "[VTO] Generated image URL: {}",
                    sanitizeForLog(imageUrl)
            );


            validateGeneratedImage(
                    imageUrl
            );


            return imageUrl;
        }


        // =========================================================
        // PROCESS_COMPLETED COMPATIBILITY
        // =========================================================

        String message =
                textValue(
                        node,
                        "msg"
                );


        if (
                "process_completed"
                        .equalsIgnoreCase(message)
        ) {

            boolean success =
                    !node.has("success")
                            ||
                            node.get("success")
                                    .asBoolean(true);


            if (!success) {

                String details =
                        extractErrorDetails(
                                eventBody
                        );


                throw new RuntimeException(
                        "Gradio inference failed: "
                                + details
                );
            }


            JsonNode outputNode =
                    node.has("output")
                            ? node.get("output")
                            : node;


            String imageUrl =
                    extractImageUrl(
                            outputNode,
                            baseUrl
                    );


            if (imageUrl == null) {

                throw new RuntimeException(
                        "Gradio process_completed event "
                                + "did not contain an image: "
                                + sanitizeForLog(eventBody)
                );
            }


            log.info(
                    "[VTO] process_completed image: {}",
                    sanitizeForLog(imageUrl)
            );


            validateGeneratedImage(
                    imageUrl
            );


            return imageUrl;
        }


        // =========================================================
        // GENERATING EVENT
        // =========================================================

        if ("generating".equals(normalizedEvent)) {

            log.info(
                    "[VTO] IDM-VTON is still generating..."
            );

            return null;
        }


        // =========================================================
        // OTHER EVENTS
        // =========================================================

        log.info(
                "[VTO] Intermediate event '{}' received; continuing",
                normalizedEvent
        );


        return null;
    }


    // =============================================================
    // EXTRACT IMAGE URL
    // =============================================================

    private String extractImageUrl(
            JsonNode node,
            String baseUrl
    ) {

        if (node == null ||
                node.isNull()) {

            return null;
        }


        /*
         * Gradio's final data is normally an array:
         *
         * [
         *   {
         *     "path": "...",
         *     "url": "...",
         *     "orig_name": "...",
         *     "meta": {
         *       "_type": "gradio.FileData"
         *     }
         *   }
         * ]
         */

        if (node.isArray()) {

            for (JsonNode item : node) {

                String url =
                        extractImageUrl(
                                item,
                                baseUrl
                        );


                if (url != null) {

                    return url;
                }
            }


            return null;
        }


        if (node.isTextual()) {

            String value =
                    node.asText();


            if (value.isBlank()) {

                return null;
            }


            return resolveGradioFileReference(
                    value,
                    baseUrl
            );
        }


        if (!node.isObject()) {

            return null;
        }


        /*
         * Prefer URL because Gradio normally provides
         * a directly accessible file URL.
         */

        for (
                String field :
                List.of(
                        "url",
                        "path",
                        "name"
                )
        ) {

            String value =
                    textValue(
                            node,
                            field
                    );


            if (
                    value != null &&
                            !value.isBlank()
            ) {

                String resolved =
                        resolveGradioFileReference(
                                value,
                                baseUrl
                        );


                if (resolved != null) {

                    return resolved;
                }
            }
        }


        /*
         * Recursively inspect nested output objects.
         */

        Iterator<JsonNode> fields =
                node.elements();


        while (fields.hasNext()) {

            JsonNode child =
                    fields.next();


            String result =
                    extractImageUrl(
                            child,
                            baseUrl
                    );


            if (result != null) {

                return result;
            }
        }


        return null;
    }


    // =============================================================
    // RESOLVE GRADIO FILE
    // =============================================================

    private String resolveGradioFileReference(
            String reference,
            String baseUrl
    ) {

        if (
                reference == null ||
                        reference.isBlank()
        ) {

            return null;
        }


        reference =
                reference.trim();


        if (
                reference.startsWith(
                        "http://"
                )
                        ||
                        reference.startsWith(
                                "https://"
                        )
        ) {

            return reference;
        }


        if (
                reference.startsWith(
                        "/file="
                )
        ) {

            return baseUrl + reference;
        }


        if (
                reference.startsWith(
                        "/"
                )
        ) {

            return baseUrl
                    + "/file="
                    + reference;
        }


        return baseUrl
                + "/file=/"
                + reference;
    }


    // =============================================================
    // VALIDATE GENERATED IMAGE
    // =============================================================

    private void validateGeneratedImage(
            String imageUrl
    ) throws Exception {

        log.info(
                "[VTO] Validating generated image..."
        );


        HttpURLConnection connection =
                (HttpURLConnection)
                        new URL(imageUrl)
                                .openConnection();


        connection.setRequestMethod(
                "GET"
        );

        connection.setConnectTimeout(
                30_000
        );

        connection.setReadTimeout(
                60_000
        );


        if (
                apiToken != null &&
                        !apiToken.isBlank()
        ) {

            connection.setRequestProperty(
                    "Authorization",
                    "Bearer " + apiToken
            );
        }


        int responseCode =
                connection.getResponseCode();


        if (
                responseCode < 200 ||
                        responseCode >= 300
        ) {

            String body =
                    readResponseBody(
                            connection.getErrorStream()
                    );


            throw new RuntimeException(
                    "Generated image download failed with HTTP "
                            + responseCode
                            + ": "
                            + sanitizeForLog(body)
            );
        }


        String contentType =
                connection.getContentType();


        byte[] imageBytes;


        try (
                InputStream stream =
                        connection.getInputStream()
        ) {

            imageBytes =
                    stream.readAllBytes();
        }


        log.info(
                "[VTO] Generated image size: {} bytes",
                imageBytes.length
        );

        log.info(
                "[VTO] Generated image Content-Type: {}",
                contentType
        );


        if (
                contentType == null ||
                        !contentType
                                .toLowerCase(
                                        Locale.ROOT
                                )
                                .startsWith(
                                        "image/"
                                )
        ) {

            throw new RuntimeException(
                    "Generated result was not an image. "
                            + "Content-Type: "
                            + contentType
            );
        }


        if (imageBytes.length == 0) {

            throw new RuntimeException(
                    "Generated image response was empty"
            );
        }


        BufferedImage image =
                ImageIO.read(
                        new ByteArrayInputStream(
                                imageBytes
                        )
                );


        if (image == null) {

            throw new RuntimeException(
                    "Generated image bytes could not be decoded"
            );
        }


        log.info(
                "[VTO] Generated image validated: {}x{}",
                image.getWidth(),
                image.getHeight()
        );
    }


    // =============================================================
    // ERROR DETAILS
    // =============================================================

    private String extractErrorDetails(
            String eventBody
    ) {

        if (
                eventBody == null ||
                        eventBody.isBlank() ||
                        "null".equalsIgnoreCase(
                                eventBody
                        )
        ) {

            return "Gradio returned an empty error event";
        }


        try {

            JsonNode node =
                    objectMapper.readTree(
                            eventBody
                    );


            for (
                    String field :
                    List.of(
                            "error",
                            "message",
                            "detail",
                            "exception",
                            "reason"
                    )
            ) {

                String value =
                        textValue(
                                node,
                                field
                        );


                if (
                        value != null &&
                                !value.isBlank()
                ) {

                    return sanitizeForLog(
                            value
                    );
                }
            }


        } catch (Exception ignored) {

            // Fall through to raw response.
        }


        return sanitizeForLog(
                eventBody
        );
    }


    // =============================================================
    // JSON HELPERS
    // =============================================================

    private String textValue(
            JsonNode node,
            String fieldName
    ) {

        if (
                node == null ||
                        !node.has(fieldName) ||
                        node.get(fieldName).isNull()
        ) {

            return null;
        }


        JsonNode value =
                node.get(fieldName);


        return value.isValueNode()
                ? value.asText()
                : null;
    }


    // =============================================================
    // RESPONSE BODY
    // =============================================================

    private String readResponseBody(
            InputStream inputStream
    ) throws Exception {

        if (inputStream == null) {

            return "<empty response body>";
        }


        return new String(
                inputStream.readAllBytes(),
                StandardCharsets.UTF_8
        );
    }


    // =============================================================
    // LOG SANITIZATION
    // =============================================================

    private String sanitizeForLog(
            String value
    ) {

        if (
                value == null ||
                        value.isBlank()
        ) {

            return "<empty>";
        }


        String sanitized =
                value.replaceAll(
                        "(?i)bearer\\s+[^\\s,}]+",
                        "Bearer [REDACTED]"
                );


        sanitized =
                sanitized.replaceAll(
                        "(?i)([?&](?:token|api[_-]?key|secret|authorization)=)[^&\\s]+",
                        "$1[REDACTED]"
                );


        return sanitized.length() > 4000
                ? sanitized.substring(
                        0,
                        4000
                ) + "..."
                : sanitized;
    }
}