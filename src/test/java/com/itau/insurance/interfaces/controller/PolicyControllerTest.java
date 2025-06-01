package com.itau.insurance.interfaces.controller;

import com.itau.insurance.application.usecases.*;
import com.itau.insurance.domain.enums.RequestStatus;
import com.itau.insurance.exceptions.NotFoundException;
import com.itau.insurance.infrastructure.persistence.entity.PolicyRequestEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PolicyControllerTest {

    private CreatePolicyUseCase createPolicyUseCase;
    private FindPolicyUseCase findPolicyUseCase;
    private ValidatePolicyUseCase validatePolicyUseCase;
    private CancelPolicyUseCase cancelPolicyUseCase;
    private PolicyController controller;

    @BeforeEach
    void setUp() {
        createPolicyUseCase = mock(CreatePolicyUseCase.class);
        findPolicyUseCase = mock(FindPolicyUseCase.class);
        validatePolicyUseCase = mock(ValidatePolicyUseCase.class);
        cancelPolicyUseCase = mock(CancelPolicyUseCase.class);

        controller = new PolicyController(
                createPolicyUseCase,
                findPolicyUseCase,
                validatePolicyUseCase,
                cancelPolicyUseCase
        );
    }

    @Test
    void deveRetornarPolicyQuandoEncontrarPorId() {
        UUID policyId = UUID.randomUUID();
        PolicyRequestEntity mockPolicy = new PolicyRequestEntity();
        mockPolicy.setId(policyId);

        when(findPolicyUseCase.findById(policyId)).thenReturn(mockPolicy);

        ResponseEntity<PolicyRequestEntity> response = controller.getById(policyId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockPolicy, response.getBody());
    }

    @Test
    void deveLancarExcecaoQuandoPolicyNaoExistir() {
        UUID policyId = UUID.randomUUID();

        when(findPolicyUseCase.findById(policyId)).thenThrow(new RuntimeException("Not found"));

        assertThrows(RuntimeException.class, () -> controller.getById(policyId));
    }

    @Test
    void deveCriarPolicyERetornarLocationECorpoComId() {
        UUID policyId = UUID.randomUUID();
        PolicyRequestEntity input = new PolicyRequestEntity();

        when(createPolicyUseCase.execute(input)).thenReturn(policyId);

        ResponseEntity<String> response = controller.create(input);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals("/policies/" + policyId, response.getHeaders().getLocation().toString());
        assertEquals("Solicitação criada com ID: " + policyId, response.getBody());
    }

    @Test
    void deveValidarPolicyEInformarStatusNoCorpoDaResposta() {
        UUID policyId = UUID.randomUUID();
        PolicyRequestEntity policy = new PolicyRequestEntity();
        policy.setId(policyId);
        policy.setStatus(RequestStatus.VALIDATED);

        when(validatePolicyUseCase.execute(policyId)).thenReturn(policy);

        ResponseEntity<String> response = controller.validatePolicy(policyId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Solicitação " + policyId + " validada com status: VALIDATED", response.getBody());
    }

    @Test
    void deveCancelarPolicyComSucesso() {
        UUID policyId = UUID.randomUUID();
        PolicyRequestEntity policy = new PolicyRequestEntity();
        policy.setId(policyId);

        when(cancelPolicyUseCase.execute(policyId)).thenReturn(policy);

        ResponseEntity<String> response = controller.cancelPolicy(policyId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Solicitação " + policyId + " cancelada com sucesso.", response.getBody());
    }
    @Test
    void deveRetornarBadRequestQuandoCancelamentoNaoEhPermitido() {
        UUID policyId = UUID.randomUUID();

        when(cancelPolicyUseCase.execute(policyId))
                .thenThrow(new IllegalStateException("Status já finalizado"));

        ResponseEntity<String> response = controller.cancelPolicy(policyId);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Cancelamento não permitido: Status já finalizado", response.getBody());
    }

    @Test
    void deveRetornarNotFoundQuandoOcorrerRuntimeException() {
        UUID policyId = UUID.randomUUID();

        when(cancelPolicyUseCase.execute(policyId))
                .thenThrow(new RuntimeException("Solicitação não encontrada"));

        ResponseEntity<String> response = controller.cancelPolicy(policyId);

        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void deveRetornarListaDePoliciesQuandoExistirem() {
        UUID customerId = UUID.randomUUID();
        List<PolicyRequestEntity> policies = List.of(new PolicyRequestEntity(), new PolicyRequestEntity());

        when(findPolicyUseCase.findByCustomerId(customerId)).thenReturn(policies);

        ResponseEntity<List<PolicyRequestEntity>> response = controller.getByCustomerId(customerId);

        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void deveRetornarNoContentQuandoNaoHouverPolicies() {
        UUID customerId = UUID.randomUUID();

        when(findPolicyUseCase.findByCustomerId(customerId)).thenReturn(Collections.emptyList());

        ResponseEntity<List<PolicyRequestEntity>> response = controller.getByCustomerId(customerId);

        assertEquals(204, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void deveRetornarPolicyPorIdQuandoExistir() {
        UUID policyId = UUID.randomUUID();
        PolicyRequestEntity policy = PolicyRequestEntity.builder()
                .id(policyId)
                .category("AUTO")
                .build();

        when(findPolicyUseCase.findById(policyId)).thenReturn(policy);

        ResponseEntity<PolicyRequestEntity> response = controller.getById(policyId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(policyId, response.getBody().getId());
        verify(findPolicyUseCase).findById(policyId);
    }

    @Test
    void deveRetornarNotFoundQuandoPolicyNaoExistir() {
        UUID policyId = UUID.randomUUID();

        when(findPolicyUseCase.findById(policyId)).thenThrow(new NotFoundException("Solicitação não encontrada"));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> controller.getById(policyId));

        assertEquals("Solicitação não encontrada", exception.getMessage());
        verify(findPolicyUseCase).findById(policyId);
    }



}
