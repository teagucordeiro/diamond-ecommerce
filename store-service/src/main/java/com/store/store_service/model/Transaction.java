package com.store.store_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Transaction {
  private String transactionId;
  private Product product;
}
