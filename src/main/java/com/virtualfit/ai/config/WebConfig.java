package com.virtualfit.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.generated-dir:generated}")
    private String generatedDir;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path generatedPath = Paths.get(generatedDir);
        String generatedAbsolutePath = generatedPath.toFile().getAbsolutePath();

        registry.addResourceHandler("/generated/**")
            .addResourceLocations("file:" + generatedAbsolutePath + "/");

        Path uploadPath = Paths.get(uploadDir);
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:" + uploadPath.toFile().getAbsolutePath() + "/");
    }
}
