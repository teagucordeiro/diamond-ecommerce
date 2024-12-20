package com.ecommerce.ecommerce_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.ecommerce.ecommerce_service.model.Bonus;
import com.ecommerce.ecommerce_service.model.BonusLog;
import com.ecommerce.ecommerce_service.repository.BonusLogRepository;

import reactor.core.publisher.Mono;

@Service
public class BonusService {
  private static final Logger LOGGER = LoggerFactory.getLogger(BonusService.class);

  private final WebClient webClient;
  private final BonusLogRepository bonusLogRepository;

  @Autowired
  public BonusService(WebClient fidelityWebClient, BonusLogRepository bonusLogRepository) {
    this.webClient = fidelityWebClient;
    this.bonusLogRepository = bonusLogRepository;
  }

  private Mono<? extends Throwable> onFidelityServiceServerError() {
    return Mono.error(new RuntimeException("Exchange Service Error"));
  }

  public Mono<String> fetchBonus(Long userId, Integer bonus) {
    return webClient.post().uri(uriBuilder -> uriBuilder.path("/bonus").build()).bodyValue(new Bonus(userId, bonus))
        .retrieve().onStatus(t -> t.is5xxServerError(), response -> onFidelityServiceServerError())
        .bodyToMono(String.class).map(response -> {
          return response;
        }).doOnError(throwable -> {
          LOGGER.error("Error trying to save bonus");
          saveLogAfterFault(userId, bonus);
        });
  }

  private void saveLogAfterFault(Long userId, Integer bonus) {
    BonusLog log = new BonusLog(null, userId, bonus, java.time.LocalDateTime.now(), false);
    bonusLogRepository.save(log);
  }

}