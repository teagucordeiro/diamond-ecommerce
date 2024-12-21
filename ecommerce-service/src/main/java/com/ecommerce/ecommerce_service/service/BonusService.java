package com.ecommerce.ecommerce_service.service;

import java.util.List;

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
    return Mono.error(new RuntimeException("Exchange Service Error - Unable to process request at the moment"));
  }

  public Mono<String> fetchBonus(Long userId, Integer bonus) {
    return webClient.post().uri(uriBuilder -> uriBuilder.path("/bonus").build()).bodyValue(new Bonus(userId, bonus))
        .retrieve().onStatus(t -> t.is5xxServerError(), response -> onFidelityServiceServerError())
        .bodyToMono(String.class).map(response -> {
          return response;
        }).doOnError(throwable -> {
          LOGGER.error("Error trying to save bonus for userId: {} with bonus: {}", userId, bonus);
          saveLogAfterFault(userId, bonus);
        }).onErrorResume(t -> Mono.empty());
  }

  public Mono<String> fetchListBonus(List<Bonus> listBonus, List<BonusLog> listBonusLog) {
    LOGGER.info("Trying to save bonus log, items: {}", listBonus.size());
    return webClient.post().uri(uriBuilder -> uriBuilder.path("/bonus/list").build()).bodyValue(listBonus).retrieve()
        .onStatus(t -> t.is5xxServerError(), response -> onFidelityServiceServerError()).bodyToMono(String.class)
        .map(response -> {
          return response;
        }).doOnSuccess(response -> saveBonusesAfterRetriedLogSuccess(listBonusLog)).doOnError(throwable -> {
          LOGGER.error("Error trying to save list log of bonus. Items: {}.", listBonus.size());
        }).onErrorResume(t -> Mono.empty());
  }

  public Mono<String> fetchBonusStatus() {
    return webClient.get().uri(uriBuilder -> uriBuilder.path("/bonus/status").build()).retrieve()
        .onStatus(t -> t.is5xxServerError(), response -> onFidelityServiceServerError()).bodyToMono(String.class)
        .map(response -> {
          return response;
        }).doOnError(throwable -> {
          LOGGER.error("Error trying to get status of bonus endpoint");
        }).onErrorResume(t -> Mono.empty());
  }

  private void saveBonusesAfterRetriedLogSuccess(List<BonusLog> listBonusLog) {
    System.out.println("Saving after sent logs with successfull");
    for (BonusLog log : listBonusLog) {
      log.setResolved(true);
      bonusLogRepository.save(log);
    }
  }

  private void saveLogAfterFault(Long userId, Integer bonus) {
    BonusLog log = new BonusLog(null, userId, bonus, java.time.LocalDateTime.now(), false);
    bonusLogRepository.save(log);
  }

}