package com.ecommerce.ecommerce_service.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecommerce.ecommerce_service.model.Product;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

@Service
public class StoreTMRService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StoreTMRService.class);

    private final ProductService productService;

    @Autowired
    public StoreTMRService(ProductService productService) {
        this.productService = productService;
    }

    public Mono<Product> getProductWithMajorityVote(String productId) {
        Mono<Product> replica1 = productService.fetchProduct(productId, 0);
        Mono<Product> replica2 = productService.fetchProduct(productId, 1);
        Mono<Product> replica3 = productService.fetchProduct(productId, 2);

        Mono<Tuple3<Product, Product, Product>> zipped = Mono.zip(replica1, replica2, replica3);
        return zipped.flatMap(tuple -> determineMajorityProduct(tuple.getT1(), tuple.getT2(), tuple.getT3(), productId))
                .doOnError(error -> LOGGER.error("Error when trying to determine the most voted product"));
    }

    private Mono<Product> determineMajorityProduct(Product r1, Product r2, Product r3, String productID) {
        List<Product> responses = new ArrayList<Product>(List.of(r1, r2, r3));
        removeNullProducts(responses);

        List<Product> matchingProducts = getProductsWithSameIDRequested(responses, productID);

        if (matchingProducts.size() < 2) {
            LOGGER.error("TMR Failure: Less than two replicas returned the same product.");
            return Mono.empty();
        }

        return Mono.just(responses.get(0));
    }

    private List<Product> getProductsWithSameIDRequested(List<Product> responses, String productID) {
        return responses.stream().filter(product -> productID.equals(product.getId())).collect(Collectors.toList());
    }

    private void removeNullProducts(List<Product> responses) {
        Product productWithNullAttributes = Product.genNullProduct();

        responses.removeIf(p -> p.equals(productWithNullAttributes));
    }
}
