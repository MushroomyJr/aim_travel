package com.aim.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.api")
public class ApiConfig {

    private String amadeusClientId;
    private String amadeusClientSecret;
    private String amadeusBaseUrl = "https://test.api.amadeus.com/v2";
    private boolean useMockData = false;
}
