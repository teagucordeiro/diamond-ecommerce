package com.ecommerce.ecommerce_service.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.ecommerce.ecommerce_service.model.Transaction;

import java.util.List;

@Service
public class StoreSellService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreSellService.class);
    private final List<WebClient> storeWebClientReplicas;

    public StoreSellService(List<WebClient> storeWebClientReplicas) {
        this.storeWebClientReplicas = storeWebClientReplicas;
    }

    @CircuitBreaker(name = "storeService", fallbackMethod = "fallbackCreateTransaction")
    @Retry(name = "storeServiceRetry")
    public String createTransaction(String productId) {
        for (WebClient webClient : storeWebClientReplicas) {
            try {
                LOGGER.info("Trying replica: {}", webClient);
                return webClient.post()
                        .uri("/sell")
                        .bodyValue(new Transaction(productId))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            } catch (Exception e) {
                LOGGER.error("Failed to communicate with replica: {}, error: {}", webClient, e.getMessage());
            }
        }

        throw new RuntimeException("All replicas failed");
    }

    private String fallbackCreateTransaction(String productId, Throwable throwable) {
        LOGGER.error("Circuit breaker activated. Fallback method called. Error: {}", throwable.getMessage());
        return "Service temporarily unavailable. Please try again later.";
    }
}