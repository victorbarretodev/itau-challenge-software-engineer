package com.itau.insurance.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itau.insurance.infrastructure.persistence.entity.PolicyRequestEntity;
import com.itau.insurance.interfaces.dto.PolicyStatusUpdateRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${messaging.exchange}")
    private String exchange;

    @Value("${messaging.routing-key}")
    private String routingKey;

    public void publishPolicyEvent(PolicyRequestEntity entity) {
        publishPolicyEvent(entity, false, false);
    }

    public void publishPolicyEvent(PolicyRequestEntity entity, boolean paymentConfirmed, boolean subscriptionAuthorized) {
        try {
            PolicyStatusUpdateRequestDTO event = new PolicyStatusUpdateRequestDTO();
            event.setPolicyId(entity.getId());
            event.setPaymentConfirmed(paymentConfirmed);
            event.setSubscriptionAuthorized(subscriptionAuthorized);

            String json = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend("policy.exchange", "policy.routing.key", json);
        } catch (Exception e) {
            log.error("Erro ao publicar evento: {}", e.getMessage(), e);
        }
    }
}
