package com.itau.insurance.application.usecases;

import com.itau.insurance.domain.enums.RequestStatus;
import com.itau.insurance.infrastructure.messaging.EventPublisher;
import com.itau.insurance.infrastructure.persistence.entity.PolicyRequestEntity;
import com.itau.insurance.infrastructure.persistence.repository.PolicyRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdatePolicyStatusUseCaseTest {

    private PolicyRequestRepository repository;
    private EventPublisher eventPublisher;
    private UpdatePolicyStatusUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(PolicyRequestRepository.class);
        eventPublisher = mock(EventPublisher.class);
        useCase = new UpdatePolicyStatusUseCase(repository, eventPublisher);
    }

    @Test
    void deveAtualizarStatusParaApprovedQuandoPagamentoEAutorizacaoForemConfirmados() {
        UUID policyId = UUID.randomUUID();
        PolicyRequestEntity policy = PolicyRequestEntity.builder()
                .id(policyId)
                .status(RequestStatus.RECEIVED)
                .history(new ArrayList<>())
                .build();

        when(repository.findById(policyId)).thenReturn(Optional.of(policy));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyRequestEntity result = useCase.execute(policyId, true, true);

        assertEquals(RequestStatus.APPROVED, result.getStatus());
        assertNotNull(result.getFinishedAt());
        assertEquals(1, result.getHistory().size());
        verify(eventPublisher).publishPolicyEvent(result, true, true);
        verify(repository).save(result);
    }

    @Test
    void deveRetornarMesmoObjetoSeStatusJaForFinal() {
        UUID policyId = UUID.randomUUID();
        PolicyRequestEntity policy = PolicyRequestEntity.builder()
                .id(policyId)
                .status(RequestStatus.REJECTED)
                .history(new ArrayList<>())
                .build();

        when(repository.findById(policyId)).thenReturn(Optional.of(policy));

        PolicyRequestEntity result = useCase.execute(policyId, false, true);

        assertEquals(RequestStatus.REJECTED, result.getStatus());
        verify(repository, never()).save(any());
        verify(eventPublisher, never()).publishPolicyEvent(any(), anyBoolean(), anyBoolean());
    }

    @Test
    void deveAtualizarParaRejectedQuandoAutorizacaoFalhar() {
        UUID id = UUID.randomUUID();
        PolicyRequestEntity policy = new PolicyRequestEntity();
        policy.setId(id);
        policy.setStatus(RequestStatus.RECEIVED);
        policy.setHistory(new ArrayList<>());

        when(repository.findById(id)).thenReturn(Optional.of(policy));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyRequestEntity result = useCase.execute(id, true, false);

        assertEquals(RequestStatus.REJECTED, result.getStatus());
        assertNotNull(result.getFinishedAt());
    }

    @Test
    void deveAtualizarParaPendingQuandoPagamentoENaoAutorizado() {
        UUID id = UUID.randomUUID();
        PolicyRequestEntity policy = new PolicyRequestEntity();
        policy.setId(id);
        policy.setStatus(RequestStatus.RECEIVED);
        policy.setHistory(new ArrayList<>());

        when(repository.findById(id)).thenReturn(Optional.of(policy));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyRequestEntity result = useCase.execute(id, false, false);

        assertEquals(RequestStatus.PENDING, result.getStatus());
        assertNull(result.getFinishedAt()); // PENDING não finaliza
    }

    @Test
    void naoDeveAtualizarSeStatusJaForFinalizado() {
        UUID id = UUID.randomUUID();
        PolicyRequestEntity policy = new PolicyRequestEntity();
        policy.setId(id);
        policy.setStatus(RequestStatus.APPROVED);

        when(repository.findById(id)).thenReturn(Optional.of(policy));

        PolicyRequestEntity result = useCase.execute(id, true, true);

        assertEquals(RequestStatus.APPROVED, result.getStatus());
        verify(repository, never()).save(any());
        verify(eventPublisher, never()).publishPolicyEvent(any(), anyBoolean(), anyBoolean());
    }

    @Test
    void naoDeveAtualizarSeNovoStatusForIgualAoAtual() {
        UUID id = UUID.randomUUID();
        PolicyRequestEntity policy = new PolicyRequestEntity();
        policy.setId(id);
        policy.setStatus(RequestStatus.REJECTED);
        policy.setHistory(new ArrayList<>());

        when(repository.findById(id)).thenReturn(Optional.of(policy));

        PolicyRequestEntity result = useCase.execute(id, true, false); // gera REJECTED novamente

        assertEquals(RequestStatus.REJECTED, result.getStatus());
        verify(repository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoSePolicyNaoForEncontrada() {
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> useCase.execute(id, true, true));
        assertEquals("Solicitação não encontrada: " + id, ex.getMessage());
    }

    @Test
    void naoDeveAtualizarSeNovoStatusForMesmoDoAtual() {
        UUID policyId = UUID.randomUUID();
        PolicyRequestEntity policy = new PolicyRequestEntity();
        policy.setId(policyId);
        policy.setStatus(RequestStatus.APPROVED); // status atual
        policy.setHistory(new ArrayList<>());

        when(repository.findById(policyId)).thenReturn(Optional.of(policy));

        // Esse input gera APPROVED novamente
        PolicyRequestEntity result = useCase.execute(policyId, true, true);

        assertEquals(RequestStatus.APPROVED, result.getStatus());

        // Verifica que não salvou nem publicou
        verify(repository, never()).save(any());
        verify(eventPublisher, never()).publishPolicyEvent(any(), anyBoolean(), anyBoolean());
    }

}
