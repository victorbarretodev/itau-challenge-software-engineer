package com.itau.insurance.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itau.insurance.application.usecases.UpdatePolicyStatusUseCase;
import com.itau.insurance.interfaces.dto.PolicyStatusUpdateRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final UpdatePolicyStatusUseCase updatePolicyStatusUseCase;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "policy.queue")
    public void receiveEvent(String messageJson) {
        try {
            PolicyStatusUpdateRequestDTO event = objectMapper.readValue(messageJson, PolicyStatusUpdateRequestDTO.class);

            updatePolicyStatusUseCase.execute(
                    event.getPolicyId(),
                    event.isPaymentConfirmed(),
                    event.isSubscriptionAuthorized()
            );

            log.info("Evento processado com sucesso para policyId {}", event.getPolicyId());

        } catch (Exception e) {
            log.error("Erro ao processar evento recebido: {}", messageJson, e);
        }
    }
}
