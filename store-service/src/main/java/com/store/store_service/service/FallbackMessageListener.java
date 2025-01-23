package com.store.store_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.store.store_service.config.RabbitConfig;
import com.store.store_service.model.Transaction;

@Service
public class FallbackMessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(FallbackMessageListener.class);

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void receiveFallbackMessage(Transaction transaction) {

        LOGGER.info("Recebendo fallback transaction no store service: {}", transaction.getTransactionId());
    }
}
