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

@Service
public class StoreTMRService {

    private final ProductService productService;

    @Autowired
    public StoreTMRService(ProductService productService) {
        this.productService = productService;
    }

    public Product getProductWithMajorityVote(String productId) {
        List<Product> productResponses = getProductResponses(productId);
        return determineMajorityProduct(productResponses);
    }

    private List<Product> getProductResponses(String productId) {
        List<Product> productResponses = new ArrayList<>();
        for (int replicaIndex = 0; replicaIndex < 3; replicaIndex++) {
            productResponses.add(fetchProductFromReplica(productId, replicaIndex));
        }
        return productResponses;
    }

    private Product fetchProductFromReplica(String productId, int replicaIndex) {
        return productService.fetchProductResponse(productId, replicaIndex);
    }

    private Product determineMajorityProduct(List<Product> responses) {
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

    private Product getProductWithHighestVote(Map<Product, Long> voteCounts) {
        return voteCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new RuntimeException("No majority response found"));
    }
}
