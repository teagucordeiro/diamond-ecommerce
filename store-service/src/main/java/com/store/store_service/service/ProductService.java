package com.store.store_service.service;

import com.store.store_service.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);

  private static final List<Product> productList = new ArrayList<>();
  private final Random random = new Random();

  static {
    productList.add(new Product("1", "Redmi Note 12", 1200.00, 10));
    productList.add(new Product("2", "MacBook Pro", 2500.00, 30));
    productList.add(new Product("3", "PlayStation 5", 500.00, 4));
  }

  public List<Product> getAllProducts() {
    return productList;
  }

  public Product getProduct(String productId) throws InterruptedException {
    if (random.nextDouble() < 0.5) {
      LOGGER.error("Simulating crash, the system will be shutdown");
      Thread.sleep(2000);
      return null;
    }

    Optional<Product> product = productList.stream().filter(p -> p.getId().equals(productId)).findFirst();
    return product.orElseThrow(() -> new RuntimeException("Produto não encontrado com o ID: " + productId));
  }

}
