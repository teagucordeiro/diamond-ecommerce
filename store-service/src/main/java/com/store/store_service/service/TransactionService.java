package com.store.store_service.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.store.store_service.model.Product;
import com.store.store_service.model.Transaction;

@Service
public class TransactionService {
  private final ProductService productService;
  private static final List<Transaction> transactionList = new ArrayList<>();

  public TransactionService(ProductService productService) {
    this.productService = productService;
  }

  public Transaction createTransaction(String productId) {
    Product product = productService.getProduct(productId);

    if (product.getQuantity() <= 0) {
      throw new RuntimeException("Product out of stock: " + product.getName());
    }

    product.setQuantity(product.getQuantity() - 1);

    Transaction transaction = new Transaction(UUID.randomUUID().toString(), product);
    transactionList.add(transaction);
    return transaction;
  }
}
