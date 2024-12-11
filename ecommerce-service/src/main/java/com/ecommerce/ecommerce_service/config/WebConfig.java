package com.ecommerce.ecommerce_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebConfig {

    private WebClient webClient(String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public WebClient storeWebClient() {
        return webClient("http://localhost:8081");
    }

    @Bean
    public WebClient exchangeWebClient() {
        return webClient("http://localhost:8082");
    }

    @Bean
    public WebClient fidelityWebClient() {
        return webClient("http://localhost:8083");
    }
}