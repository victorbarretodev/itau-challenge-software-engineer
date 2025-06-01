package com.itau.insurance.application.usecases;

import com.itau.insurance.domain.enums.RequestStatus;
import com.itau.insurance.exceptions.BusinessException;
import com.itau.insurance.infrastructure.messaging.EventPublisher;
import com.itau.insurance.infrastructure.persistence.entity.PolicyRequestEntity;
import com.itau.insurance.infrastructure.persistence.repository.PolicyRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CancelPolicyUseCaseTest {

    private PolicyRequestRepository repository;
    private EventPublisher eventPublisher;
    private CancelPolicyUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(PolicyRequestRepository.class);
        eventPublisher = mock(EventPublisher.class);
        useCase = new CancelPolicyUseCase(repository, eventPublisher);
    }

    @Test
    void deveCancelarApliceSeNaoEstiverEmStatusFinal() {
        UUID policyId = UUID.randomUUID();
        PolicyRequestEntity policy = new PolicyRequestEntity();
        policy.setId(policyId);
        policy.setStatus(RequestStatus.RECEIVED);
        policy.setCreatedAt(LocalDateTime.now());

        when(repository.findById(policyId)).thenReturn(Optional.of(policy));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyRequestEntity result = useCase.execute(policyId);

        assertEquals(RequestStatus.CANCELLED, result.getStatus());
        assertNotNull(result.getFinishedAt());
        verify(eventPublisher).publishPolicyEvent(result);
        verify(repository).save(result);
    }

    @Test
    void deveLancarExcecaoSeStatusForFinal() {
        UUID policyId = UUID.randomUUID();
        PolicyRequestEntity policy = new PolicyRequestEntity();
        policy.setId(policyId);
        policy.setStatus(RequestStatus.APPROVED); // status final

        when(repository.findById(policyId)).thenReturn(Optional.of(policy));

        BusinessException ex = assertThrows(BusinessException.class, () -> useCase.execute(policyId));
        assertTrue(ex.getMessage().contains("Solicitação não pode ser cancelada"));
    }
}
