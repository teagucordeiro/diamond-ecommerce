package com.exchange.exchange_service.service;

import java.time.Instant;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.exchange.exchange_service.model.Exchange;

@Service
public class ExchangeRateService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeRateService.class);

    private final Random random = new Random();

    public Double getExchangeRate() {
        double min = 5.8;
        double max = 6.5;
        return min + (max - min) * random.nextDouble();
    }

    private void simulateCrash() {
        if (random.nextDouble() < 0.1) {
            LOGGER.error("Simulating crash, the system will be shutdown");
            System.exit(1);
        }
    }

    public Exchange getExchangeRateResponse() {
        Double rate = getExchangeRate();
        LOGGER.info("Gen rate: ", rate);

        simulateCrash();

        return new Exchange(
                "USD",
                "BRL",
                rate,
                Instant.now().getEpochSecond());
    }
}
