package com.itau.insurance.application.usecases;

import com.itau.insurance.domain.enums.RequestStatus;
import com.itau.insurance.infrastructure.messaging.EventPublisher;
import com.itau.insurance.infrastructure.persistence.entity.PolicyRequestEntity;
import com.itau.insurance.infrastructure.persistence.entity.StatusHistoryEntity;
import com.itau.insurance.infrastructure.persistence.repository.PolicyRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdatePolicyStatusUseCase {

    private final PolicyRequestRepository repository;
    private final EventPublisher eventPublisher;

    public PolicyRequestEntity execute(UUID policyId, boolean paymentConfirmed, boolean subscriptionAuthorized) {
        PolicyRequestEntity policy = repository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada: " + policyId));

        if (isFinalState(policy.getStatus())) {
            return policy; // não atualiza se já estiver finalizado
        }

        RequestStatus newStatus;
        if (!paymentConfirmed && !subscriptionAuthorized) {
            newStatus = RequestStatus.PENDING;
        } else if (paymentConfirmed && subscriptionAuthorized) {
            newStatus = RequestStatus.APPROVED;
        } else {
            newStatus = RequestStatus.REJECTED;
        }

        if (policy.getStatus() == newStatus) {
            return policy; // evita redundância
        }

        policy.setStatus(newStatus);

        if (isFinalState(newStatus)) {
            policy.setFinishedAt(LocalDateTime.now());
        }

        policy.getHistory().add(StatusHistoryEntity.builder()
                .status(newStatus)
                .timestamp(LocalDateTime.now())
                .build());

        PolicyRequestEntity saved = repository.save(policy);
        eventPublisher.publishPolicyEvent(saved, paymentConfirmed, subscriptionAuthorized);

        return saved;
    }

    private boolean isFinalState(RequestStatus status) {
        return status == RequestStatus.CANCELLED
                || status == RequestStatus.REJECTED
                || status == RequestStatus.APPROVED;
    }
}
