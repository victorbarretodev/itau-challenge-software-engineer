package com.itau.insurance.application.usecases;

import com.itau.insurance.domain.enums.RequestStatus;
import com.itau.insurance.exceptions.BusinessException;
import com.itau.insurance.exceptions.NotFoundException;
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
public class CancelPolicyUseCase {

    private final PolicyRequestRepository repository;
    private final EventPublisher eventPublisher;

    public PolicyRequestEntity execute(UUID policyId) {
        PolicyRequestEntity policy = repository.findById(policyId)
                .orElseThrow(() -> new NotFoundException("Solicitação não encontrada: " + policyId));

        if (isFinalStatus(policy.getStatus())) {
            throw new BusinessException("Solicitação não pode ser cancelada pois está em status final: " + policy.getStatus());
        }

        policy.setStatus(RequestStatus.CANCELLED);
        policy.setFinishedAt(LocalDateTime.now());

        policy.getHistory().add(StatusHistoryEntity.builder()
                .status(RequestStatus.CANCELLED)
                .timestamp(LocalDateTime.now())
                .build());

        eventPublisher.publishPolicyEvent(policy);

        return repository.save(policy);
    }

    private boolean isFinalStatus(RequestStatus status) {
        return status == RequestStatus.APPROVED || status == RequestStatus.REJECTED;
    }
}
