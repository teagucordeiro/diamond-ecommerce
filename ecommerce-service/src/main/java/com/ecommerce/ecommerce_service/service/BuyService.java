package com.ecommerce.ecommerce_service.service;

import org.springframework.stereotype.Service;

import com.ecommerce.ecommerce_service.model.Exchange;
import com.ecommerce.ecommerce_service.model.Product;

@Service
public class BuyService {
    private final ExchangeService exchangeService;
    private final StoreTMRService storeTMRService;
    private final BonusService bonusService;
    private final StoreSellService storeSellService;

    public BuyService(ExchangeService exchangeService, StoreTMRService storeTMRService, BonusService bonusService,
            StoreSellService storeSellService) {
        this.exchangeService = exchangeService;
        this.storeTMRService = storeTMRService;
        this.bonusService = bonusService;
        this.storeSellService = storeSellService;
    }

    private Double calcProductPrice(double productPrice, double exchangeRate) {
        return productPrice * exchangeRate;
    }

    public String buyProduct(String productID, Boolean isFaultToleranceEnabled, Long userID) {
        Product product = storeTMRService.getProductWithMajorityVote(productID, isFaultToleranceEnabled).block();

        if (product == null) {
            return null;
        }

        if (product.getId().equals("null id")) {
            return null;
        }

        Exchange exchange = exchangeService.fetchExchangeResponse(isFaultToleranceEnabled);

        if (exchange.getIsCached() == null) {
            exchange.setIsCached(false);
        }

        Double productPriceCalcWithExchangeRate = calcProductPrice(product.getValue(), exchange.getRate());

        String sellResponse = storeSellService.createTransaction(productID, product);

        Integer bonus = productPriceCalcWithExchangeRate.intValue();
        String bonusResponse = bonusService.fetchBonus(userID, bonus, isFaultToleranceEnabled).block();

        return buildTransactionOutput(product, productPriceCalcWithExchangeRate, exchange, sellResponse, bonusResponse);
    }

    private String buildExchangeResponseString(Exchange exchange) {
        Boolean isExchangeChached = exchange.getIsCached();

        if (isExchangeChached) {
            return "Taxa de Câmbio Atual (cached): " + exchange.getRate();
        }

        return "Taxa de Câmbio Atual: " + exchange.getRate();
    }

    private String buildSellResponseString(String sellResponse) {

        return "Id da venda: " + sellResponse;
    }

    private String buildTransactionOutput(Product product, Double productPriceXExchange, Exchange exchange,
            String sellResponse, String bonusResponse) {
        if (exchange == null) {
            return "No exchange service response";
        }
        if (sellResponse == null || sellResponse == "Service temporarily unavailable. Please try again later.") {
            return "No sell service response";
        }

        return "Compra efetuada com sucesso!" + "\n" + buildExchangeResponseString(exchange) + "\n"
                + buildSellResponseString(sellResponse) + "\n" + "Bonus Response: " + bonusResponse;
    }
}
