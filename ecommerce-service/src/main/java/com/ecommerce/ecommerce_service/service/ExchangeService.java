package com.ecommerce.ecommerce_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.ecommerce.ecommerce_service.model.Exchange;

import reactor.core.publisher.Mono;

@Service
public class ExchangeService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeService.class);
  
  private final WebClient webClient;
  private Exchange cachedExchange;

  @Autowired
  public ExchangeService(WebClient exchangeWebClient) {
    this.webClient = exchangeWebClient;
  }

  private void saveLastExchange(Exchange lastExchange) {
    this.cachedExchange = lastExchange;
  }

  public Mono<Exchange> fetchExchange() {
    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/exchange")
            .build())
        .retrieve()
        .onStatus(t -> t.is5xxServerError(), response -> Mono.error(
            new RuntimeException("Exchange Service Error")))
        .bodyToMono(Exchange.class).map(response -> {
          saveLastExchange(response);
          return response;
        }).retry(1)
        .doOnError(error -> {
          System.err.println("Failed to get exchange rate: " + error.getMessage());
        });
  }

  public Exchange getLastKnownRate() {
    return cachedExchange;
  }

  public Exchange fetchExchangeResponse() {
    Mono<Exchange> responseMono = fetchExchange().onErrorResume(ex -> {
      Exchange fallbackExchange = getLastKnownRate();
      if (fallbackExchange != null) {
        LOGGER.warn("Exchange service failure. Using cached value.");
        return Mono.just(fallbackExchange);
      } else {
        return Mono.error(new RuntimeException("No last known rate available", ex));
      }
    });
    ;

    return responseMono.block();
  }
}
