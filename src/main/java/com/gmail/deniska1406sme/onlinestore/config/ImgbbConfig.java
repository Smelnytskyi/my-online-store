package com.gmail.deniska1406sme.onlinestore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImgbbConfig {

    @Value("${imgbb.api.key}")
    private String apiKey;

    @Value("${imgbb.api.url}")
    private String apiUrl;

    public String getApiKey() {
        return apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }
}
