package com.hnust.easyweibo.backend.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class WebConfig implements WebMvcConfigurer {

    private final AppProperties appProperties;

    public WebConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
            .allowedMethods("*")
            .allowedHeaders("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(appProperties.getUploadDir()).toAbsolutePath();
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations(uploadPath.toUri().toString());
    }
}
