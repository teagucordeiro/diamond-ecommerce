package com.ecommerce.ecommerce_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionRequest {
    private String productId;
}