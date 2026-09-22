package com.virtualfit.ai.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "try_on_results")
public class TryOnResult {
    
    @Id
    private String id;
    
    private String userId;
    private String personImage;
    private String garmentImage;
    private String generatedImage;
    private String garmentCategory;
    private String garmentDescription;
    private String status;
    
    @CreatedDate
    private LocalDateTime createdAt;

    public TryOnResult() {}

    public TryOnResult(String id, String userId, String personImage, String garmentImage, String generatedImage, String status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.personImage = personImage;
        this.garmentImage = garmentImage;
        this.generatedImage = generatedImage;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getPersonImage() { return personImage; }
    public void setPersonImage(String personImage) { this.personImage = personImage; }
    
    public String getGarmentImage() { return garmentImage; }
    public void setGarmentImage(String garmentImage) { this.garmentImage = garmentImage; }
    
    public String getGeneratedImage() { return generatedImage; }
    public void setGeneratedImage(String generatedImage) { this.generatedImage = generatedImage; }

    public String getGarmentCategory() { return garmentCategory; }
    public void setGarmentCategory(String garmentCategory) { this.garmentCategory = garmentCategory; }

    public String getGarmentDescription() { return garmentDescription; }
    public void setGarmentDescription(String garmentDescription) { this.garmentDescription = garmentDescription; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static TryOnResultBuilder builder() {
        return new TryOnResultBuilder();
    }

    public static class TryOnResultBuilder {
        private String id;
        private String userId;
        private String personImage;
        private String garmentImage;
        private String generatedImage;
        private String status;
        private LocalDateTime createdAt;

        public TryOnResultBuilder id(String id) { this.id = id; return this; }
        public TryOnResultBuilder userId(String userId) { this.userId = userId; return this; }
        public TryOnResultBuilder personImage(String personImage) { this.personImage = personImage; return this; }
        public TryOnResultBuilder garmentImage(String garmentImage) { this.garmentImage = garmentImage; return this; }
        public TryOnResultBuilder generatedImage(String generatedImage) { this.generatedImage = generatedImage; return this; }
        public TryOnResultBuilder status(String status) { this.status = status; return this; }
        public TryOnResultBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public TryOnResult build() { return new TryOnResult(id, userId, personImage, garmentImage, generatedImage, status, createdAt); }
    }
}
