package com.store.store_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Product {
  private String id;
  private String name;
  private double value;
  private int quantity;
}
