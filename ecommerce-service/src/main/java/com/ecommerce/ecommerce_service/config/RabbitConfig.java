package com.ecommerce.ecommerce_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String QUEUE_NAME = "transactionsFallbackQueue";
    public static final String EXCHANGE_NAME = "transactionsFallbackExchange";
    public static final String ROUTING_KEY = "transactionsFallbackKey";

    @Bean
    public Queue transactionsFallbackQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public TopicExchange transactionsFallbackExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue transactionsFallbackQueue, TopicExchange transactionsFallbackExchange) {
        return BindingBuilder.bind(transactionsFallbackQueue).to(transactionsFallbackExchange).with(ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
