package com.exchange.exchange_service.service;

import java.time.Instant;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.exchange.exchange_service.model.Exchange;

@Service
public class ExchangeRateService {

    private final Random random = new Random();

    public Double getExchangeRate() {
        double min = 5.8;
        double max = 6.2;
        return min + (max - min) * random.nextDouble();
    }

    public Exchange getExchangeRateResponse() {
        Double rate = getExchangeRate();
        return new Exchange(
                "USD",
                "BRL",
                rate,
                Instant.now().getEpochSecond()
        );
    }
}
