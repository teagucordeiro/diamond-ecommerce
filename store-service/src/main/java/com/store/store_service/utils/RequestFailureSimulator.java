package com.store.store_service.utils;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;;

public class RequestFailureSimulator {
  private AtomicBoolean inErrorState = new AtomicBoolean(false);
  private long lastErrorTimestamp = 0;
  private static final long ERROR_DURATION_MS = 5000;
  private final Random random = new Random();

  private boolean isWithinErrorDuration(long currentTime) {
    return (currentTime - lastErrorTimestamp) < ERROR_DURATION_MS;
  }

  private boolean shouldEnterErrorState(double probability) {
    return random.nextDouble() < probability;
  }

  private synchronized void enterErrorState(long currentTime) {
    inErrorState.set(true);
    lastErrorTimestamp = currentTime;
  }

  public boolean shouldFail(double probability) {
    long currentTime = System.currentTimeMillis();

    if (inErrorState.get() && isWithinErrorDuration(currentTime)) {
      return true;
    }

    if (inErrorState.get() && !isWithinErrorDuration(currentTime)) {
      inErrorState.set(false);
    }

    if (!inErrorState.get() && shouldEnterErrorState(probability)) {
      enterErrorState(currentTime);
    }

    return inErrorState.get();
  }
}
