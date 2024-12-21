package com.ecommerce.ecommerce_service.jobs;

import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ecommerce.ecommerce_service.model.Bonus;
import com.ecommerce.ecommerce_service.model.BonusLog;
import com.ecommerce.ecommerce_service.repository.BonusLogRepository;
import com.ecommerce.ecommerce_service.service.BonusService;

@Component
@EnableScheduling
public class BonusRetryTask {
  private final BonusService bonusService;
  private final BonusLogRepository failureLogRepository;

  public BonusRetryTask(BonusService bonusService, BonusLogRepository failureLogRepository) {
    this.bonusService = bonusService;
    this.failureLogRepository = failureLogRepository;
  }

  @Scheduled(fixedDelay = 30 * 1000) // 30 seconds
  public void retryFailedRequests() {
    var failedLogs = failureLogRepository.findByResolvedFalse();

    if (failedLogs.size() > 0) {
      String bonusEndpointStatus = bonusService.fetchBonusStatus().block();
      System.out.println("bonusStatus: " + bonusEndpointStatus);

      if (bonusEndpointStatus.equals("up")) {
        List<Bonus> bonusToTrySaveAgain = new ArrayList<Bonus>();
        for (BonusLog bonusItem : failedLogs) {
          bonusToTrySaveAgain.add(new Bonus(bonusItem.getUserId(), bonusItem.getBonus()));
        }

        bonusService.fetchListBonus(bonusToTrySaveAgain, failedLogs).block();
      }
    }
  }
}
