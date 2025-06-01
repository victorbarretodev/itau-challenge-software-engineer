package com.itau.insurance.application.usecases;

import com.itau.insurance.domain.enums.RequestStatus;
import com.itau.insurance.exceptions.PolicyCreationException;
import com.itau.insurance.infrastructure.messaging.EventPublisher;
import com.itau.insurance.infrastructure.persistence.entity.PolicyRequestEntity;
import com.itau.insurance.infrastructure.persistence.entity.StatusHistoryEntity;
import com.itau.insurance.infrastructure.persistence.repository.PolicyRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreatePolicyUseCaseTest {

    private PolicyRequestRepository repository;
    private EventPublisher eventPublisher;
    private CreatePolicyUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(PolicyRequestRepository.class);
        eventPublisher = mock(EventPublisher.class);
        useCase = new CreatePolicyUseCase(repository, eventPublisher);
    }

    @Test
    void deveCriarSolicitacaoComStatusInicialEPublicarEvento() {
        // Arrange
        PolicyRequestEntity input = PolicyRequestEntity.builder()
                .customerId(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .category("VIDA")
                .salesChannel("APP")
                .paymentMethod("PIX")
                .insuredAmount(new BigDecimal("500000"))
                .totalMonthlyPremiumAmount(new BigDecimal("350"))
                .history(new ArrayList<>())
                .build();

        PolicyRequestEntity saved = PolicyRequestEntity.builder()
                .id(UUID.randomUUID())
                .customerId(input.getCustomerId())
                .productId(input.getProductId())
                .category(input.getCategory())
                .salesChannel(input.getSalesChannel())
                .paymentMethod(input.getPaymentMethod())
                .insuredAmount(input.getInsuredAmount())
                .totalMonthlyPremiumAmount(input.getTotalMonthlyPremiumAmount())
                .status(RequestStatus.RECEIVED)
                .createdAt(LocalDateTime.now())
                .history(new ArrayList<>())
                .build();

        when(repository.save(any())).thenReturn(saved);

        // Act
        UUID resultId = useCase.execute(input);

        // Assert
        assertNotNull(resultId);
        assertEquals(RequestStatus.RECEIVED, input.getStatus());
        assertNotNull(input.getCreatedAt());
        assertEquals(1, input.getHistory().size());
        assertEquals(RequestStatus.RECEIVED, input.getHistory().get(0).getStatus());

        verify(repository).save(input);
        verify(eventPublisher).publishPolicyEvent(saved);
    }

    @Test
    void deveLancarExcecaoQuandoRequestForNulo() {
        assertThrows(PolicyCreationException.class, () -> useCase.execute(null));
    }

    @Test
    void deveLancarExcecaoQuandoFalhaAoSalvarPolicy() {
        PolicyRequestEntity policy = PolicyRequestEntity.builder()
                .customerId(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .category("VIDA")
                .salesChannel("MOBILE")
                .paymentMethod("CREDIT_CARD")
                .insuredAmount(BigDecimal.valueOf(300_000))
                .totalMonthlyPremiumAmount(BigDecimal.valueOf(100.0))
                .coverages(new HashMap<>())
                .assistances(new ArrayList<>())
                .history(new ArrayList<>())
                .build();

        when(repository.save(any())).thenThrow(new RuntimeException("DB error"));

        PolicyCreationException ex = assertThrows(PolicyCreationException.class, () -> useCase.execute(policy));
        assertTrue(ex.getMessage().contains("Erro ao criar a solicitação"));
    }


}
