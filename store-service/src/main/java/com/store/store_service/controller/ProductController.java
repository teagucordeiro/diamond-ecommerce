package com.store.store_service.controller;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.store.store_service.model.Product;
import com.store.store_service.service.ProductService;

@RestController
@RequestMapping("/store")
public class ProductController {
  private final ProductService productService;
  private final Random random = new Random();

  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @GetMapping("/product")
  public ResponseEntity<List<Product>> getAllProducts() {
    return ResponseEntity.ok(productService.getAllProducts());
  }

  @GetMapping("/product/{productId}")
  public CompletableFuture<ResponseEntity<Product>> getProduct(@PathVariable String productId) {
    if (random.nextDouble() < 0.2) {
      return new CompletableFuture<>();
    }

    return CompletableFuture.supplyAsync(() -> { 
      try {
      Product productDetails = productService.getProduct(productId);
      return ResponseEntity.ok(productDetails);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }});
  }
}