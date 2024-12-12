package com.ecommerce.ecommerce_service.service;

import org.springframework.stereotype.Service;

import com.ecommerce.ecommerce_service.model.Exchange;
import com.ecommerce.ecommerce_service.model.Product;

@Service
public class BuyService {
    private final ProductService productService;
    private final ExchangeService exchangeService;

    public BuyService(ProductService productService, ExchangeService exchangeService) {
        this.productService = productService;
        this.exchangeService = exchangeService;
    }

    private Double calcProductPrice(double productPrice, double exchangeRate) {
        return productPrice * exchangeRate;
    }

    public String buyProduct(String productID) {
        Product product = productService.fetchProductResponse(productID);
        Exchange exchange = exchangeService.fetchExchangeResponse();
        Double productPriceCalcWithExchangeRate = calcProductPrice(product.getValue(), exchange.getRate());

        return "Product: " + product.getName() + "\ninit price: " + product.getValue() + "\nAfter exchange rate calc: "
                + productPriceCalcWithExchangeRate;
    }
}
