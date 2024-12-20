package com.ecommerce.ecommerce_service.service;

import org.springframework.stereotype.Service;

import com.ecommerce.ecommerce_service.model.Exchange;
import com.ecommerce.ecommerce_service.model.Product;

import reactor.core.publisher.Mono;

@Service
public class BuyService {
    private final ExchangeService exchangeService;
    private final StoreTMRService storeTMRService;
    private final BonusService bonusService;

    public BuyService(ExchangeService exchangeService, StoreTMRService storeTMRService, BonusService bonusService) {
        this.exchangeService = exchangeService;
        this.storeTMRService = storeTMRService;
        this.bonusService = bonusService;
    }

    private Double calcProductPrice(double productPrice, double exchangeRate) {
        return productPrice * exchangeRate;
    }

    public String buyProduct(String productID, Long userID) {
        Product product = storeTMRService.getProductWithMajorityVote(productID).block();

        if (product == null) {
            return null;
        }

        Exchange exchange = exchangeService.fetchExchangeResponse();
        Double productPriceCalcWithExchangeRate = calcProductPrice(product.getValue(), exchange.getRate());
        Mono<String> bonusResponse = bonusService.fetchBonus(userID, 1);

        return buildTransactionOutput(product, productPriceCalcWithExchangeRate, exchange);
    }

    private String buildTransactionOutput(Product product, Double productPriceXExchange, Exchange exchange) {
        return String.format(
                "Produto adquirido com sucesso!\n" + "=============================\n" + "Nome: %s\n"
                        + "Preço Original (BRL): R$ %.2f\n" + "Taxa de Câmbio Atual: %.2f\n"
                        + "Preço Convertido (USD): $ %.2f\n" + "=============================",
                product.getName(), product.getValue(), exchange.getRate(), productPriceXExchange);
    }
}
