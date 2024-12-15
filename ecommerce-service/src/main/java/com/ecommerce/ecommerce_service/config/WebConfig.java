package com.ecommerce.ecommerce_service.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;

@Configuration
public class WebConfig {

    @Autowired
    private Environment environment;

    HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(5));

    private WebClient webClient(String baseUrl) {
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public WebClient storeWebClient() {
        return webClient(environment.getProperty("STORE_URL", "without-url-store") + "/store");
    }

    @Bean
    public WebClient exchangeWebClient() {
        return webClient(environment.getProperty("EXCHANGE_URL", "without-url-exchange"));
    }

    @Bean
    public WebClient fidelityWebClient() {
        return webClient("http://localhost:8083");
    }
}