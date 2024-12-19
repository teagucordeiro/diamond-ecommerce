package com.fidelity.fidelity_service.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fidelity.fidelity_service.model.UserModel;

@Service
public class BonusService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BonusService.class);

    private static final Double FAILURE_PROBABILITY = 0.2;
    private static final long FAILURE_DURATION_MS = 30 * 1000;
    private static final long RESPONSE_DELAY_MS = 2000;

    private final AtomicBoolean inFailureMode = new AtomicBoolean(false);
    private long failureStartTime = 0;

    private final Random random = new Random();

    private List<UserModel> users = new ArrayList<UserModel>();

    @Autowired
    public BonusService(List<UserModel> users) {
        this.users = users;
    }

    public UserModel findUser(Long userID) {
        Optional<UserModel> userOptional = users.stream().filter(user -> user.getId().equals(userID)).findFirst();

        if (userOptional.isPresent()) {
            return userOptional.get();
        }
        LOGGER.warn("User not finded: " + userID);
        return null;
    }

    private void createUserWithBonus(Long userID, Integer bonus) {
        users.add(new UserModel(userID, bonus));
    }

    private Boolean isSimulatingFailure() throws InterruptedException {
        if (inFailureMode.get()) {
            Boolean isFailureTimeHasExpired = Instant.now().toEpochMilli() - failureStartTime >= FAILURE_DURATION_MS;

            if (isFailureTimeHasExpired) {
                inFailureMode.set(false);
                return false;
            }

            Thread.sleep(RESPONSE_DELAY_MS);
            LOGGER.warn("The service took 2 seconds to respond");
            return true;
        }

        if (!inFailureMode.get() && random.nextDouble() < FAILURE_PROBABILITY) {
            inFailureMode.set(true);
            failureStartTime = Instant.now().toEpochMilli();
            Thread.sleep(RESPONSE_DELAY_MS);
            LOGGER.warn("The service took 2 seconds to respond");
            return true;
        }
        return false;
    }

    public List<UserModel> getAllUsers() {
        return users;
    }

    public void processBonus(Long userId, Integer bonus) throws InterruptedException {
        if (isSimulatingFailure()) {
            return;
        }

        UserModel user = findUser(userId);

        if (user == null) {
            createUserWithBonus(userId, bonus);
        } else {
            user.setBonus(user.getBonus() + bonus);
        }
    }
}