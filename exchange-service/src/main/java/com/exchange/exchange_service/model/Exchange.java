package com.exchange.exchange_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Exchange {
    private String baseCurrency;
    private String targetCurrency;
    private Double rate;
    private Long timestamp;
}
