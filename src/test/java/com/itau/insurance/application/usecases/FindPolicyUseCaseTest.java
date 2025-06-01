package com.itau.insurance.application.usecases;

import com.itau.insurance.exceptions.PolicyNotFoundException;
import com.itau.insurance.infrastructure.persistence.entity.PolicyRequestEntity;
import com.itau.insurance.infrastructure.persistence.repository.PolicyRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FindPolicyUseCaseTest {

    private PolicyRequestRepository repository;
    private FindPolicyUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(PolicyRequestRepository.class);
        useCase = new FindPolicyUseCase(repository);
    }

    @Test
    void deveRetornarPolicyPorId() {
        UUID id = UUID.randomUUID();
        PolicyRequestEntity expected = PolicyRequestEntity.builder().id(id).build();

        when(repository.findById(id)).thenReturn(Optional.of(expected));

        Optional<PolicyRequestEntity> result = Optional.ofNullable(useCase.findById(id));

        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }

    @Test
    void deveRetornarListaDePoliciesPorCustomerId() {
        UUID customerId = UUID.randomUUID();
        List<PolicyRequestEntity> list = List.of(
                PolicyRequestEntity.builder().customerId(customerId).build(),
                PolicyRequestEntity.builder().customerId(customerId).build()
        );

        when(repository.findByCustomerId(customerId)).thenReturn(list);

        List<PolicyRequestEntity> result = useCase.findByCustomerId(customerId);

        assertEquals(2, result.size());
        assertEquals(customerId, result.get(0).getCustomerId());
    }

    @Test
    void deveLancarExcecaoQuandoPolicyNaoEncontradaPorId() {
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(PolicyNotFoundException.class, () -> useCase.findById(id));
    }

    @Test
    void deveLancarExcecaoQuandoNenhumaPolicyEncontradaPorCustomerId() {
        UUID customerId = UUID.randomUUID();

        when(repository.findByCustomerId(customerId)).thenReturn(Collections.emptyList());

        assertThrows(PolicyNotFoundException.class, () -> useCase.findByCustomerId(customerId));
    }



}
