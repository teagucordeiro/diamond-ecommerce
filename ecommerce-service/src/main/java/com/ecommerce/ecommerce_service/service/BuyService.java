package com.ecommerce.ecommerce_service.service;

import org.springframework.stereotype.Service;

import com.ecommerce.ecommerce_service.model.Product;

@Service
public class BuyService {
    private final ExchangeService exchangeService;
    private final StoreTMRService storeTMRService;

    public BuyService(ExchangeService exchangeService, StoreTMRService storeTMRService) {
        this.exchangeService = exchangeService;
        this.storeTMRService = storeTMRService;
    }

    private Double calcProductPrice(double productPrice, double exchangeRate) {
        return productPrice * exchangeRate;
    }

    public String buyProduct(String productID) {
        Product product = storeTMRService.getProductWithMajorityVote(productID).block();
        // Exchange exchange = exchangeService.fetchExchangeResponse();
        // Double productPriceCalcWithExchangeRate = calcProductPrice(product.getValue(), 6.3);

        return "Product:" + product.getName();
    }
}
