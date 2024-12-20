package com.store.store_service.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.store.store_service.model.Product;

@Service
public class ProductService {
  private static final List<Product> productList = new ArrayList<>();

  static {
    productList.add(new Product("1", "Redmi Note 12", 1200.00, 10));
    productList.add(new Product("2", "MacBook Pro", 2500.00, 30));
    productList.add(new Product("3", "PlayStation 5", 500.00, 4));
  }

  public List<Product> getAllProducts() {
    return productList;
  }

  public Product getProduct(String productId) {
    Optional<Product> product = productList.stream().filter(p -> p.getId().equals(productId)).findFirst();
    return product.orElseThrow(() -> new RuntimeException("Produto não encontrado com o ID: " + productId));
  }

}
