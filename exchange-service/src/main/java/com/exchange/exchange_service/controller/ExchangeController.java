package com.exchange.exchange_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.exchange_service.model.Exchange;
import com.exchange.exchange_service.service.ExchangeRateService;

@RestController
@RequestMapping("/exchange")
public class ExchangeController {

    private final ExchangeRateService exchangeRateService;

    @Autowired
    public ExchangeController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping
    public ResponseEntity<Exchange> getExchangeRate() {

        Exchange response = exchangeRateService.getExchangeRateResponse();
        return ResponseEntity.ok(response);
    }
}