package com.itau.insurance.application.usecases;

import com.itau.insurance.domain.enums.RequestStatus;
import com.itau.insurance.exceptions.PolicyCreationException;
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
public class CreatePolicyUseCase {

    private final PolicyRequestRepository repository;
    private final EventPublisher eventPublisher;

    public UUID execute(PolicyRequestEntity request) {
        try {
            if (request == null) {
                throw new PolicyCreationException("A solicitação de apólice está nula");
            }

            request.setStatus(RequestStatus.RECEIVED);
            request.setCreatedAt(LocalDateTime.now());

            StatusHistoryEntity initialStatus = StatusHistoryEntity.builder()
                    .status(RequestStatus.RECEIVED)
                    .timestamp(request.getCreatedAt())
                    .build();

            request.getHistory().add(initialStatus);

            PolicyRequestEntity saved = repository.save(request);

            eventPublisher.publishPolicyEvent(saved);

            return saved.getId();
        } catch (Exception e) {
            throw new PolicyCreationException("Erro ao criar a solicitação de apólice", e);
        }
    }
}
