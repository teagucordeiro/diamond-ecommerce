package com.ecommerce.ecommerce_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Product {
    private String id;
    private String name;
    private double value;

    public static Product genNullProduct() {
        return new Product("null id", "null product", 0);
    }

    @Override
    public String toString() {
        return "Product{" + "id=" + id + ", name='" + name + '\'' + ", price=" + value + '}';
    }
}