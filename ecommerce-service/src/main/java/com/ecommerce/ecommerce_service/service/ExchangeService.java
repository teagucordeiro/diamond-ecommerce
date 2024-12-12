package com.ecommerce.ecommerce_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.ecommerce.ecommerce_service.model.Exchange;

import reactor.core.publisher.Mono;

@Service
public class ExchangeService {
  private final WebClient webClient;

  @Autowired
  public ExchangeService(WebClient exchangeWebClient) {
    this.webClient = exchangeWebClient;
  }

  public Mono<Exchange> fetchExchange() {
    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/exchange")
            .build())
        .retrieve()
        .bodyToMono(Exchange.class)
        .doOnError(error -> {
          System.err.println("Falha ao obter cotação: " + error.getMessage());
        });
  }

  public Exchange fetchExchangeResponse() {
    Mono<Exchange> responseMono = fetchExchange();

    return responseMono.block();
  }
}
