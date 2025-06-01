package com.itau.insurance.interfaces.controller;

import com.itau.insurance.application.usecases.UpdatePolicyStatusUseCase;
import com.itau.insurance.interfaces.dto.PolicyStatusEventRequestDTO;
import com.itau.insurance.infrastructure.persistence.entity.PolicyRequestEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final UpdatePolicyStatusUseCase updatePolicyStatusUseCase;

    @PostMapping("/status")
    public ResponseEntity<String> processStatusEvent(@RequestBody PolicyStatusEventRequestDTO request) {
        PolicyRequestEntity updated = updatePolicyStatusUseCase.execute(
                request.getId(),
                request.isPaymentConfirmed(),
                request.isSubscriptionAuthorized()
        );

        return ResponseEntity.ok("Status atualizado para: " + updated.getStatus());
    }
}
