package com.virtualfit.ai.service;

import com.virtualfit.ai.model.TryOnResult;
import com.virtualfit.ai.model.User;
import com.virtualfit.ai.repository.TryOnResultRepository;
import com.virtualfit.ai.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
public class StyleFitService {
    private static final Logger log = LoggerFactory.getLogger(StyleFitService.class);


    private final HuggingFaceService huggingFaceService;
    private final TryOnResultRepository tryOnResultRepository;
    private final UserRepository userRepository;

    public StyleFitService(HuggingFaceService huggingFaceService, TryOnResultRepository tryOnResultRepository, UserRepository userRepository) {
        this.huggingFaceService = huggingFaceService;
        this.tryOnResultRepository = tryOnResultRepository;
        this.userRepository = userRepository;
    }


    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.generated-dir:generated}")
    private String generatedDir;

    public TryOnResult processTryOn(String userEmail, MultipartFile personImage, MultipartFile garmentImage, String category, String description) throws Exception {
        
        // 1. Generate Image from HuggingFace
        String resultImageUrl = huggingFaceService.generateTryOn(personImage, garmentImage, description);

        // 2. Download and save the generated image locally
        String savedPersonPath = saveFileLocally(personImage, "person_");
        String savedGarmentPath = saveFileLocally(garmentImage, "garment_");
        String savedResultPath = downloadAndSaveImage(resultImageUrl, "tryon_");

        // 3. Save to database if user is authenticated
        TryOnResult tryOnResult = new TryOnResult();
        tryOnResult.setPersonImage("/uploads/" + savedPersonPath);
        tryOnResult.setGarmentImage("/uploads/" + savedGarmentPath);
        tryOnResult.setGeneratedImage("/generated/" + savedResultPath);
        tryOnResult.setGarmentCategory(category);
        tryOnResult.setGarmentDescription(description);
        tryOnResult.setCreatedAt(LocalDateTime.now());
        tryOnResult.setStatus("completed");

        if (userEmail != null && !userEmail.isEmpty()) {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            tryOnResult.setUserId(user.getId());
            tryOnResultRepository.save(tryOnResult);
        }

        return tryOnResult;
    }

    private String saveFileLocally(MultipartFile file, String prefix) throws IOException {
        String filename = prefix + UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path targetPath = Paths.get(uploadDir, filename).toAbsolutePath();
        Files.createDirectories(targetPath.getParent());
        file.transferTo(targetPath.toFile());
        return filename;
    }

    private String downloadAndSaveImage(String imageUrl, String prefix) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        String filename = prefix + UUID.randomUUID() + ".png";
        Path targetPath = Paths.get(generatedDir, filename).toAbsolutePath();
        Files.createDirectories(targetPath.getParent());

        restTemplate.execute(imageUrl, HttpMethod.GET, null, clientHttpResponse -> {
                String contentType = clientHttpResponse.getHeaders().getFirst("Content-Type");
                if (!clientHttpResponse.getStatusCode().is2xxSuccessful()
                    || contentType == null
                    || !contentType.toLowerCase().startsWith("image/")) {
                throw new IOException("Generated result download did not return an image");
            }
            File ret = targetPath.toFile();
            try (FileOutputStream fos = new FileOutputStream(ret)) {
                StreamUtils.copy(clientHttpResponse.getBody(), fos);
            }
            return ret;
        });
        
        // Validate the downloaded file
        File downloadedFile = targetPath.toFile();
        if (!downloadedFile.exists() || downloadedFile.length() == 0) {
            downloadedFile.delete();
            throw new RuntimeException("Downloaded image is empty");
        }
        
        try {
            java.awt.image.BufferedImage testImage = javax.imageio.ImageIO.read(downloadedFile);
            if (testImage == null) {
                log.error("========================================");
                log.error("INVALID IDM-VTON RESULT");
                log.error("========================================");
                log.error("Bytes: {}", downloadedFile.length());
                log.error("Reason: ImageIO could not decode the file.");
                log.error("========================================");
                downloadedFile.delete();
                throw new RuntimeException("Downloaded file is not a valid image");
            }
            
            log.info("========================================");
            log.info("IDM-VTON IMAGE VALIDATION");
            log.info("========================================");
            log.info("Downloaded bytes: {}", downloadedFile.length());
            log.info("Valid image: YES");
            log.info("Image dimensions: {} x {}", testImage.getWidth(), testImage.getHeight());
            log.info("Saved file: {}", "/generated/" + filename);
            log.info("Browser URL: /generated/{}", filename);
            log.info("========================================");
            
        } catch (Exception e) {
            log.error("========================================");
            log.error("INVALID IDM-VTON RESULT");
            log.error("========================================");
            log.error("Bytes: {}", downloadedFile.length());
            log.error("Reason: {}", e.getMessage());
            log.error("========================================");
            downloadedFile.delete();
            throw new RuntimeException("Downloaded file is not a valid image", e);
        }

        return filename;
    }
}
