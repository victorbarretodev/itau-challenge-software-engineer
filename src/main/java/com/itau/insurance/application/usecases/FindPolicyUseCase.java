package com.itau.insurance.application.usecases;

import com.itau.insurance.exceptions.PolicyNotFoundException;
import com.itau.insurance.infrastructure.persistence.entity.PolicyRequestEntity;
import com.itau.insurance.infrastructure.persistence.repository.PolicyRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindPolicyUseCase {

    private final PolicyRequestRepository repository;

    public PolicyRequestEntity findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new PolicyNotFoundException(id));
    }

    public List<PolicyRequestEntity> findByCustomerId(UUID customerId) {
        List<PolicyRequestEntity> policies = repository.findByCustomerId(customerId);

        if (policies.isEmpty()) {
            throw new PolicyNotFoundException("Nenhuma apólice encontrada para o cliente " + customerId);
        }

        return policies;
    }
}
