package com.ecommerce.ecommerce_service.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.ecommerce.ecommerce_service.model.Product;

import reactor.core.publisher.Mono;

@Service
public class ProductService {
  private List<WebClient> storeWebClientReplicas;
  private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);

  @Autowired
  public ProductService(List<WebClient> storeWebClientReplicas) {
    this.storeWebClientReplicas = storeWebClientReplicas;
  }

  public Mono<Product> fetchProduct(String id, Integer replica) {
    return storeWebClientReplicas.get(replica).get().uri(uriBuilder -> uriBuilder.path("/product/" + id).build())
        .retrieve().bodyToMono(Product.class).onErrorResume(e -> {
          LOGGER.error("Error fetching product from replica: " + replica + " error: " + e.getMessage());
          return Mono.just(Product.genNullProduct());
        });
  }
}
