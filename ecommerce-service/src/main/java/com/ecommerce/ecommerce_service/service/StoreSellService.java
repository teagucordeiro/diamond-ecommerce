package com.ecommerce.ecommerce_service.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Autowired
    public StoreSellService(@Qualifier("storeWebClient")List<WebClient> storeWebClientReplicas,
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
        // Corrigido para acessar storeWebClientReplicas
        for (WebClient webClient : storeWebClientReplicas) {
            try {
                return webClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/store/sell/status").build())
                        .retrieve()
                        .onStatus(t -> t.is5xxServerError(), response -> onSellServiceServerError())
                        .bodyToMono(String.class)
                        .map(response -> response)
                        .doOnError(throwable -> LOGGER.error("Error trying to get status of store sell endpoint"))
                        .onErrorResume(t -> Mono.empty());
            } catch (Exception e) {
                LOGGER.error("Failed to communicate with replica: {}", webClient, e);
            }
        }

        return Mono.error(new RuntimeException("All replicas failed to fetch store sell status"));
    }

    private void saveTransactionsAfterRetriedLogSuccess(List<TransactionLog> listTransactionLog) {
        System.out.println("Saving after sent logs with sucessfull");
        for (TransactionLog log: listTransactionLog) {
            log.setResolved(true);
            transactionLogRepository.save(log);
        }
    }

    public Mono<String> fetchListTrasaction(List<TransactionLog> listTrasactionLog, List<TransactionLog> listFailedLog) {
        LOGGER.info("Trying to save transaction log, items: {}", listTrasactionLog.size());
        
        // Itera sobre as réplicas, mas retorna assim que uma réplica disponível for encontrada
        return Mono.defer(() -> {
            for (WebClient webClient : storeWebClientReplicas) {
                try {
                    // Tenta fazer a requisição para a réplica
                    return webClient.post()
                            .uri(uriBuilder -> uriBuilder.path("/store/sell/transactions-list").build())
                            .bodyValue(listTrasactionLog)
                            .retrieve()
                            .onStatus(t -> t.is5xxServerError(), response -> onSellServiceServerError())
                            .bodyToMono(String.class)
                            .map(response -> {
                                // Se a requisição for bem-sucedida, salva os logs de transações
                                saveTransactionsAfterRetriedLogSuccess(listFailedLog);
                                return response;
                            })
                            .doOnError(throwable -> {
                                // Loga erro se ocorrer durante a requisição
                                LOGGER.error("Error trying to save transaction log of bonus. Items: {}.", listTrasactionLog.size());
                            })
                            .onErrorResume(t -> Mono.empty());
                } catch (Exception e) {
                    LOGGER.error("Failed to communicate with replica: {}", webClient, e);
                }
            }
    
            // Caso todas as réplicas falhem, você pode lançar uma exceção ou retornar um fallback
            return Mono.error(new RuntimeException("All replicas failed to save transaction logs"));
        });
    }
}
