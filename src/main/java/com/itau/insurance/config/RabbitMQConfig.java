package com.itau.insurance.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange policyExchange() {
        return new TopicExchange("policy.exchange");
    }

    @Bean
    public Queue policyQueue() {
        return QueueBuilder.durable("policy.queue")
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", "policy.dlq")
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue("policy.dlq");
    }



    @Bean
    public Binding binding(Queue policyQueue, TopicExchange policyExchange) {
        return BindingBuilder.bind(policyQueue).to(policyExchange).with("policy.status.updated");
    }
}
