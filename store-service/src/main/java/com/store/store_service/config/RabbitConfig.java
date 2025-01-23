package com.store.store_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String QUEUE_NAME = "transactionsFallbackQueue";
    public static final String EXCHANGE_NAME = "transactionsFallbackExchange";
    public static final String ROUTING_KEY = "transactionsFallbackKey";

    @Bean
    public Queue fallbackQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public TopicExchange fallbackExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue fallbackQueue, TopicExchange fallbackExchange) {
        return BindingBuilder.bind(fallbackQueue).to(fallbackExchange).with(ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
