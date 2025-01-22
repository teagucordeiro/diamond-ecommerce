package com.ecommerce.ecommerce_service.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.ecommerce.ecommerce_service.model.Product;
import com.ecommerce.ecommerce_service.model.Transaction;
import com.ecommerce.ecommerce_service.model.TransactionLog;
import com.ecommerce.ecommerce_service.model.TransactionRequest;
import com.ecommerce.ecommerce_service.repository.TransactionLogRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class StoreSellService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreSellService.class);
    private final List<WebClient> storeWebClientReplicas;
    private final TransactionLogRepository transactionLogRepository;

    public StoreSellService(List<WebClient> storeWebClientReplicas,
            TransactionLogRepository transactionLogRepository) {
        this.storeWebClientReplicas = storeWebClientReplicas;
        this.transactionLogRepository = transactionLogRepository;
    }

    private Mono<? extends Throwable> onSellServiceServerError() {
        return Mono.error(new RuntimeException("Store Sell Service Error - Unable to process request at the moment"));
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

        TransactionLog transactionLog = new TransactionLog();

        transactionLog.setTransactionId(transactionFallback.getTransactionId());
        transactionLog.setProductId(productId);
        transactionLog.setTimestamp(LocalDateTime.now());

        transactionLogRepository.save(transactionLog);

        LOGGER.warn("Returning fallback transaction ID: {}", transactionFallback.getTransactionId());
        return "fallbackID-" + transactionFallback.getTransactionId();
    }

    public Mono<String> fetchStoreSellStatus() {
        for (WebClient webClient : storeWebClientReplicas) {
            try {
                String status = webClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/sell/status").build())
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
    
                if ("up".equalsIgnoreCase(status)) {  
                    LOGGER.info("Replica is UP: {}", webClient);
                    return Mono.just(webClient.toString());
                }
            } catch (Exception e) {
                LOGGER.error("Failed to communicate with replica: {}", webClient, e);
            }
        }
    
        return Mono.error(new RuntimeException("No available replicas to fetch store sell status"));
    }

    private void saveTransactionsAfterRetriedLogSuccess(List<TransactionLog> listTransactionLog) {
        System.out.println("Saving after sent logs with sucessfull");
        for (TransactionLog log : listTransactionLog) {
            log.setResolved(true);
            transactionLogRepository.save(log);
        }
    }

    public Mono<String> fetchListTrasaction(List<TransactionLog> listTransactionLog, List<TransactionLog> listFailedLog) {
        LOGGER.info("Trying to save transaction log, items: {}", listTransactionLog.size());
    
        return Mono.defer(() -> {
            return fetchStoreSellStatus().flatMap(replicaUri -> {
                for (WebClient webClient : storeWebClientReplicas) {
                    if (webClient.toString().equals(replicaUri)) { 
                        LOGGER.info("Sending logs to replica: {}", replicaUri);
                        
                        return webClient.post()
                                .uri(uriBuilder -> uriBuilder.path("/sell/transactions-list").build())
                                .bodyValue(listTransactionLog)
                                .retrieve()
                                .onStatus(t -> t.is5xxServerError(), response -> onSellServiceServerError())
                                .bodyToMono(String.class)
                                .map(response -> {
                                    saveTransactionsAfterRetriedLogSuccess(listFailedLog);
                                    return response;
                                })
                                .doOnError(throwable -> {
                                    LOGGER.error("Error trying to save transaction log of bonus. Items: {}.",
                                            listTransactionLog.size());
                                })
                                .onErrorResume(t -> Mono.empty());
                    }
                }
    
                return Mono.error(new RuntimeException("No matching replica found to send the transaction logs"));
            });
        });
    }
}
