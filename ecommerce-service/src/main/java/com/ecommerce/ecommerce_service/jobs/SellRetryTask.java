package com.ecommerce.ecommerce_service.jobs;

import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ecommerce.ecommerce_service.model.TransactionLog;
import com.ecommerce.ecommerce_service.repository.TransactionLogRepository;
import com.ecommerce.ecommerce_service.service.StoreSellService;

@Component
@EnableScheduling
public class SellRetryTask {
  private final StoreSellService storeSellService;
  private final TransactionLogRepository failureLogRepository;

  public SellRetryTask(StoreSellService storeSellService, TransactionLogRepository failureLogRepository) {
    this.storeSellService = storeSellService;
    this.failureLogRepository = failureLogRepository;
  }

  private void sentLogsToTryPostSellAgain(String sellEndpointStatus, List<TransactionLog> failedLogs) {
    if (sellEndpointStatus.equals("up")) {
      List<TransactionLog> transactionTOTrySaveAgain = new ArrayList<TransactionLog>();
      for (TransactionLog transactionLog : failedLogs) {
        transactionTOTrySaveAgain.add(transactionLog);
      }

      storeSellService.fetchListTrasaction(transactionTOTrySaveAgain, failedLogs).block();
    }
  }

  @Scheduled(fixedDelay = 300 * 1000)
  public void retryFailedRequests() {
    var failedLogs = failureLogRepository.findByResolvedFalse();

    if (failedLogs.size() > 0) {
      String storeSellEndpointStatus = storeSellService.fetchStoreSellStatus().block();
      System.out.println("Store sell status: " + storeSellEndpointStatus);

      sentLogsToTryPostSellAgain(storeSellEndpointStatus, null);
    }
  }
}
