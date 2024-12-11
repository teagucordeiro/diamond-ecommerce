package com.ecommerce.ecommerce_service.service;

import com.ecommerce.ecommerce_service.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class BuyService {
    private final WebClient webClient;

    @Autowired
    public BuyService(WebClient storeWebClient) {
        this.webClient = storeWebClient;
    }

    public Mono<Product> fetchProduct(String id) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/product")
                        .queryParam("product", id)
                        .build())
                .retrieve()
                .bodyToMono(Product.class)
                .doOnError(error -> {
                    System.err.println("Falha ao obter produto: " + error.getMessage());
                });
    }

    public Product fetchProductResponse(String id) {
        Mono<Product> responseMono = fetchProduct("2234");

        return responseMono.block();
    }
}
