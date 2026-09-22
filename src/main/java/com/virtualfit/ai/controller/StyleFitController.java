package com.virtualfit.ai.controller;

import com.virtualfit.ai.dto.TryOnResponse;
import com.virtualfit.ai.model.TryOnResult;
import com.virtualfit.ai.service.StyleFitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/tryon")
public class StyleFitController {
    private static final Logger log = LoggerFactory.getLogger(StyleFitController.class);


    private final StyleFitService styleFitService;

    public StyleFitController(StyleFitService styleFitService) {
        this.styleFitService = styleFitService;
    }


    @PostMapping
    public ResponseEntity<TryOnResponse> tryOn(
            @RequestParam("personImage") MultipartFile personImage,
            @RequestParam("garmentImage") MultipartFile garmentImage,
            @RequestParam(value = "garmentCategory", defaultValue = "upper_body") String garmentCategory,
            @RequestParam(value = "garmentDescription", defaultValue = "garment") String garmentDescription) {
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = null;
            if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
                userEmail = authentication.getName();
            }

            log.info("\n========================================\n" +
                     "STYLEFIT TRY-ON REQUEST\n" +
                     "========================================\n" +
                     "Person image received: {}\n" +
                     "Garment image received: {}\n" +
                     "Category: {}\n" +
                     "Description: {}\n" +
                     "Calling IDM-VTON...",
                     personImage != null && !personImage.isEmpty() ? "YES" : "NO",
                     garmentImage != null && !garmentImage.isEmpty() ? "YES" : "NO",
                     garmentCategory,
                     garmentDescription);

            TryOnResult result = styleFitService.processTryOn(userEmail, personImage, garmentImage, garmentCategory, garmentDescription);

            log.info("Result received: {}\n" +
                     "Generated image: {}\n" +
                     "========================================",
                     result != null && result.getGeneratedImage() != null ? "YES" : "NO",
                     result != null ? result.getGeneratedImage() : "null");

            return ResponseEntity.ok(TryOnResponse.builder()
                    .success(true)
                    .status(result == null ? "completed" : result.getStatus())
                    .resultImage(result.getGeneratedImage())
                    .imageUrl(result.getGeneratedImage())
                    .build());

        } catch (Exception e) {
                log.error("Try-on failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(TryOnResponse.builder()
                    .success(false)
                    .error("IDM-VTON inference failed")
                    .message(safeErrorMessage(e.getMessage()))
                    .build());
        }
    }

    private String safeErrorMessage(String message) {
        if (message == null || message.isBlank()) {
            return "StyleFit could not generate the result.";
        }
        String safeMessage = message
                .replaceAll("(?i)bearer\\s+[^\\s,}]+", "Bearer [REDACTED]")
                .replaceAll("/(?:Users|private|tmp|var)/[^\\s,}]+", "[path]");
        return safeMessage.length() > 500 ? safeMessage.substring(0, 500) + "..." : safeMessage;
    }
}
