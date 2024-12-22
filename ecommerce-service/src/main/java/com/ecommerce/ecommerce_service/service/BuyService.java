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

    public BuyService(ExchangeService exchangeService, StoreTMRService storeTMRService, 
                      BonusService bonusService, StoreSellService storeSellService) {
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
        Double productPriceCalcWithExchangeRate = calcProductPrice(product.getValue(), exchange.getRate());

        String sellResponse = storeSellService.createTransaction(productID);

        Integer bonus = productPriceCalcWithExchangeRate.intValue();
        String bonusResponse = bonusService.fetchBonus(userID, bonus, isFaultToleranceEnabled).block();

        return buildTransactionOutput(product, productPriceCalcWithExchangeRate, exchange, sellResponse, bonusResponse);
    }

    private String buildTransactionOutput(Product product, Double productPriceXExchange, Exchange exchange,
            String sellResponse, String bonusResponse) {
        return String.format(
                "Produto adquirido com sucesso!\n" + "=============================\n" + "Nome: %s\n"
                        + "Preço Original (BRL): R$ %.2f\n" + "Taxa de Câmbio Atual: %.2f\n"
                        + "Preço Convertido (USD): $ %.2f\n" + "Registro da venda: %s\n" + "Bonus: %s\n" + "=============================",
                product.getName(), product.getValue(), exchange.getRate(), productPriceXExchange, sellResponse, bonusResponse);
    }
}
