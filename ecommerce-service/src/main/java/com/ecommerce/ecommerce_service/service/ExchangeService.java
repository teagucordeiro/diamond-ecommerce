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

  private Mono<Exchange> onErrorResume(Boolean isFaultToleranceEnabled) {
    if (!isFaultToleranceEnabled) {
      LOGGER.error("Exchange error: Fault tolerance is disabled. Returning empty response.");
      return Mono.empty();
    }
    Exchange fallbackExchange = getLastKnownRate();

    if (fallbackExchange != null) {
      LOGGER.warn("Exchange service failed. Using cached exchange rate: {}", fallbackExchange);
      return Mono.just(fallbackExchange);
    } else {
      LOGGER.error("No last known exchange rate available for fallback.");
      return Mono.error(new RuntimeException("No last known exchange rate available for fallback."));
    }
  }

  private Mono<? extends Throwable> onServerError() {
    LOGGER.error("Exchange service returned a 5xx server error.");
    return Mono.error(new RuntimeException("Exchange service returned a 5xx server error"));
  }

  private Integer getNumberOfRetries(Boolean isFaultToleranceEnabled) {
    if (isFaultToleranceEnabled) {
      return 1;
    }
    return 0;
  }

  private void doOnError(Throwable error) {
    LOGGER.error("Failed to fetch exchange rate: {}", error.getMessage());
  }

  public Mono<Exchange> fetchExchange(Boolean isFaultToleranceEnabled) {
    return webClient.get().uri(uriBuilder -> uriBuilder.path("/exchange").build()).retrieve()
        .onStatus(t -> t.is5xxServerError(), response -> onServerError()).bodyToMono(Exchange.class).map(response -> {
          saveLastExchange(response);
          return response;
        }).retry(getNumberOfRetries(isFaultToleranceEnabled)).doOnError(error -> doOnError(error))
        .onErrorResume(ex -> onErrorResume(isFaultToleranceEnabled));
  }

  public Exchange getLastKnownRate() {
    return cachedExchange;
  }

  public Exchange fetchExchangeResponse(Boolean isFaultToleranceEnabled) {
    Mono<Exchange> responseMono = fetchExchange(isFaultToleranceEnabled);

    return responseMono.block();
  }
}
