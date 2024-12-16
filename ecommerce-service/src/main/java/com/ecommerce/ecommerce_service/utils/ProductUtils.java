package com.ecommerce.ecommerce_service.utils;

import java.util.List;

import com.ecommerce.ecommerce_service.model.Product;

public class ProductUtils {
  public static void printProductNames(List<Product> products) {
    if (products == null || products.isEmpty()) {
      System.out.println("A lista de produtos está vazia ou é nula.");
      return;
    }

    System.out.println("Nomes dos produtos:");
    for (Product product : products) {
      if (product == null) {
        System.out.println("Produto nulo encontrado.");
      } else if (product.getName() == null) {
        System.out.println("Produto com nome nulo encontrado. ID: " + product.getId());
      } else {
        System.out.println(product.getName());
      }
    }
  }
}
