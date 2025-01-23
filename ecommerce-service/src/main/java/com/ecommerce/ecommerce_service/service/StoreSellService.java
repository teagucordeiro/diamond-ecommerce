package com.ecommerce.ecommerce_service.service;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.ecommerce.ecommerce_service.model.Product;
import com.ecommerce.ecommerce_service.model.Transaction;
import com.ecommerce.ecommerce_service.model.TransactionRequest;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class StoreSellService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreSellService.class);

    private final List<WebClient> storeWebClientReplicas;
    private final RabbitMQService rabbitMQService;

    public StoreSellService(List<WebClient> storeWebClientReplicas, RabbitMQService rabbitMQService) {
        this.storeWebClientReplicas = storeWebClientReplicas;
        this.rabbitMQService = rabbitMQService;
    }

    @CircuitBreaker(name = "storeService", fallbackMethod = "fallbackCreateTransaction")
    @Retry(name = "storeServiceRetry")
    public String createTransaction(String productId, Product product) {
        for (WebClient webClient : storeWebClientReplicas) {
            try {
                LOGGER.info("Trying replica: {}", webClient);
                return webClient.post().uri("/sell").bodyValue(new TransactionRequest(productId)).retrieve()
                        .bodyToMono(String.class).block();
            } catch (Exception e) {
                LOGGER.error("Failed to communicate with replica: {}", webClient, e);
            }
        }

        throw new RuntimeException("All replicas failed");
    }

    private String fallbackCreateTransaction(String productId, Product product, Throwable throwable) {
        LOGGER.error("Circuit breaker activated. Fallback method called. Cause: {}", throwable.getMessage());
        Transaction transactionFallback = new Transaction(UUID.randomUUID().toString(), product);

        LOGGER.warn("Returning fallback transaction ID: {}", transactionFallback.getTransactionId());
        rabbitMQService.sendTransaction(transactionFallback);

        return "fallbackID-" + transactionFallback.getTransactionId();
    }
}
