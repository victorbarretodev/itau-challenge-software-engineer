package com.itau.insurance.interfaces.controller;

import com.itau.insurance.application.usecases.UpdatePolicyStatusUseCase;
import com.itau.insurance.domain.enums.RequestStatus;
import com.itau.insurance.infrastructure.persistence.entity.PolicyRequestEntity;
import com.itau.insurance.interfaces.dto.PolicyStatusEventRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventControllerTest {

    private UpdatePolicyStatusUseCase updatePolicyStatusUseCase;
    private EventController controller;

    @BeforeEach
    void setUp() {
        updatePolicyStatusUseCase = mock(UpdatePolicyStatusUseCase.class);
        controller = new EventController(updatePolicyStatusUseCase);
    }

    @Test
    void deveAtualizarStatusComSucessoERetornarMensagem() {
        UUID id = UUID.randomUUID();
        PolicyStatusEventRequestDTO request = new PolicyStatusEventRequestDTO();
        request.setId(id);
        request.setPaymentConfirmed(true);
        request.setSubscriptionAuthorized(true);

        PolicyRequestEntity entity = new PolicyRequestEntity();
        entity.setStatus(RequestStatus.APPROVED);

        when(updatePolicyStatusUseCase.execute(
                request.getId(),
                request.isPaymentConfirmed(),
                request.isSubscriptionAuthorized())
        ).thenReturn(entity);

        ResponseEntity<String> response = controller.processStatusEvent(request);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Status atualizado para: APPROVED"));
        verify(updatePolicyStatusUseCase).execute(id, true, true);
    }
}
