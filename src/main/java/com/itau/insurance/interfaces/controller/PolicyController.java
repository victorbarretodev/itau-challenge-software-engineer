package com.itau.insurance.interfaces.controller;

import com.itau.insurance.application.usecases.CancelPolicyUseCase;
import com.itau.insurance.application.usecases.CreatePolicyUseCase;
import com.itau.insurance.application.usecases.FindPolicyUseCase;
import com.itau.insurance.application.usecases.ValidatePolicyUseCase;
import com.itau.insurance.infrastructure.persistence.entity.PolicyRequestEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/policies")
@Tag(name = "Policy Requests", description = "Gerenciamento de solicitações de apólices")
@RequiredArgsConstructor
public class PolicyController {

    private final CreatePolicyUseCase createPolicyUseCase;
    private final FindPolicyUseCase findPolicyUseCase;
    private final ValidatePolicyUseCase validatePolicyUseCase;
    private final CancelPolicyUseCase cancelPolicyUseCase;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody PolicyRequestEntity request) {
        UUID id = createPolicyUseCase.execute(request);

        return ResponseEntity.created(URI.create("/policies/" + id))
                .body("Solicitação criada com ID: " + id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PolicyRequestEntity> getById(@PathVariable UUID id) {
        PolicyRequestEntity policy = findPolicyUseCase.findById(id);
        return ResponseEntity.ok(policy);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PolicyRequestEntity>> getByCustomerId(@PathVariable UUID customerId) {
        List<PolicyRequestEntity> policies = findPolicyUseCase.findByCustomerId(customerId);

        if (policies.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(policies);
    }

    @PostMapping("/{id}/validate")
    public ResponseEntity<String> validatePolicy(@PathVariable UUID id) {
        PolicyRequestEntity updated = validatePolicyUseCase.execute(id);

        return ResponseEntity.ok("Solicitação " + id + " validada com status: " + updated.getStatus());
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<String> cancelPolicy(@PathVariable UUID id) {
        try {
            PolicyRequestEntity updated = cancelPolicyUseCase.execute(id);
            return ResponseEntity.ok("Solicitação " + id + " cancelada com sucesso.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body("Cancelamento não permitido: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
