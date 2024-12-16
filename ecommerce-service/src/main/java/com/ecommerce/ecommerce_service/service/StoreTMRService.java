package com.ecommerce.ecommerce_service.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecommerce.ecommerce_service.model.Product;

import reactor.core.publisher.Mono;

@Service
public class StoreTMRService {

    private final ProductService productService;

    @Autowired
    public StoreTMRService(ProductService productService) {
        this.productService = productService;
    }

    public Mono<Product> getProductWithMajorityVote(String productId) {
        Mono<Product> replica1 = productService.fetchProduct(productId, 0);
        Mono<Product> replica2 = productService.fetchProduct(productId, 1);
        Mono<Product> replica3 = productService.fetchProduct(productId, 2);

        return Mono.zip(replica1, replica2, replica3).flatMap(data -> {
            List<Product> productResponses = List.of(data.getT1(), data.getT2(), data.getT3());
            System.out.println(data.getT1().getName());
            System.out.println(data.getT2().getName());
            System.out.println(data.getT3().getName());
            return determineMajorityProduct(productResponses);
        });
    }

    private Mono<Product> determineMajorityProduct(List<Product> responses) {
        Map<Product, Long> productVoteCounts = countProductVotes(responses);
        return getProductWithHighestVote(productVoteCounts);
    }

    private Map<Product, Long> countProductVotes(List<Product> responses) {
        return filterNonNullProducts(responses)
                .collect(Collectors.groupingBy(product -> product, Collectors.counting()));
    }

    private Stream<Product> filterNonNullProducts(List<Product> responses) {
        return responses.stream().filter(Objects::nonNull);
    }

    private Mono<Product> getProductWithHighestVote(Map<Product, Long> voteCounts) {
        return Mono.just(voteCounts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                .orElseThrow(() -> new RuntimeException("No majority response found")));
    }
}
