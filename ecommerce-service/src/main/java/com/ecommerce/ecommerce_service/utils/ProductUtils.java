package com.ecommerce.ecommerce_service.utils;

import java.util.List;

import com.ecommerce.ecommerce_service.model.Product;

public class ProductUtils {
  public static void printProductNames(List<Product> products) {
    if (products == null || products.isEmpty()) {
      System.out.println("The product list is empty or null.");
      return;
    }

    System.out.println("Product names:");
    for (Product product : products) {
      if (product == null) {
        System.out.println("Null product found.");
      } else if (product.getName() == null) {
        System.out.println("Product with null name found. ID: " + product.getId());
      } else {
        System.out.println(product.getName());
      }
    }
  }
}
