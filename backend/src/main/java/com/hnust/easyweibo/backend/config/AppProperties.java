package com.hnust.easyweibo.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Jwt jwt = new Jwt();
    private final Gemini gemini = new Gemini();
    private String uploadDir = "uploads";

    public Jwt getJwt() {
        return jwt;
    }

    public Gemini getGemini() {
        return gemini;
    }

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public static class Jwt {
        private String secret;
        private long expireHours = 72;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getExpireHours() {
            return expireHours;
        }

        public void setExpireHours(long expireHours) {
            this.expireHours = expireHours;
        }
    }

    public static class Gemini {
        private String apiKey;
        private String model = "gemini-2.5-flash-lite";

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }
    }
}
