package com.store.store_service.utils;

import java.util.Random;

public class RequestFailureSimulator {
  private boolean inErrorState = false;
  private long lastErrorTimestamp = 0;
  private static final long ERROR_DURATION_MS = 5000;
  private final Random random = new Random();

  public boolean shouldFail(double probability) {
    long currentTime = System.currentTimeMillis();

    if (inErrorState && (currentTime - lastErrorTimestamp) < ERROR_DURATION_MS) {
      return true;
    }

    if (inErrorState && (currentTime - lastErrorTimestamp) >= ERROR_DURATION_MS) {
      inErrorState = false;
    }

    if (!inErrorState && random.nextDouble() < probability) {
      inErrorState = true;
      lastErrorTimestamp = currentTime;
    }

    return inErrorState;
  }
}
