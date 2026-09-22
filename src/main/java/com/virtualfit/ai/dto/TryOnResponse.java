package com.virtualfit.ai.dto;

public class TryOnResponse {
    private boolean success;
    private String status;
    private String resultImage;
    private String imageUrl;
    private String error;
    private String message;

    public TryOnResponse() {}

    public TryOnResponse(boolean success, String status, String resultImage, String error, String message) {
        this.success = success;
        this.status = status;
        this.resultImage = resultImage;
        this.error = error;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getResultImage() { return resultImage; }
    public void setResultImage(String resultImage) { this.resultImage = resultImage; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public static TryOnResponseBuilder builder() {
        return new TryOnResponseBuilder();
    }

    public static class TryOnResponseBuilder {
        private boolean success;
        private String status;
        private String resultImage;
        private String imageUrl;
        private String error;
        private String message;

        public TryOnResponseBuilder success(boolean success) { this.success = success; return this; }
        public TryOnResponseBuilder status(String status) { this.status = status; return this; }
        public TryOnResponseBuilder resultImage(String resultImage) { this.resultImage = resultImage; return this; }
        public TryOnResponseBuilder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }
        public TryOnResponseBuilder error(String error) { this.error = error; return this; }
        public TryOnResponseBuilder message(String message) { this.message = message; return this; }
        
        public TryOnResponse build() {
            TryOnResponse response = new TryOnResponse(success, status, resultImage, error, message);
            response.setImageUrl(imageUrl);
            return response;
        }
    }
}
