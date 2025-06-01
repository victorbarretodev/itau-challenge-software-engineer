package com.itau.insurance.application.usecases;

import com.itau.insurance.domain.enums.RequestStatus;
import com.itau.insurance.domain.enums.RiskClassification;
import com.itau.insurance.exceptions.FraudAnalysisException;
import com.itau.insurance.exceptions.InvalidRiskClassificationException;
import com.itau.insurance.exceptions.PolicyNotFoundException;
import com.itau.insurance.infrastructure.external.fraud.FraudApiClient;
import com.itau.insurance.infrastructure.external.fraud.FraudAnalysisResponse;
import com.itau.insurance.infrastructure.messaging.EventPublisher;
import com.itau.insurance.infrastructure.persistence.entity.PolicyRequestEntity;
import com.itau.insurance.infrastructure.persistence.entity.StatusHistoryEntity;
import com.itau.insurance.infrastructure.persistence.repository.PolicyRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ValidatePolicyUseCase {

    private final FraudApiClient fraudApiClient;
    private final PolicyRequestRepository repository;
    private final EventPublisher eventPublisher;

    public PolicyRequestEntity execute(UUID policyId) {
        PolicyRequestEntity policy = repository.findById(policyId)
                .orElseThrow(() -> new PolicyNotFoundException(policyId));

        try {
            FraudAnalysisResponse response = fraudApiClient.analyze(policy.getId());

            RiskClassification classification = response.getClassification();
            policy.setStatus(shouldApprove(policy, classification) ? RequestStatus.VALIDATED : RequestStatus.REJECTED);

            policy.getHistory().add(StatusHistoryEntity.builder()
                    .status(policy.getStatus())
                    .timestamp(LocalDateTime.now())
                    .build());

            eventPublisher.publishPolicyEvent(policy);

            return repository.save(policy);
        } catch (Exception ex) {
            throw new FraudAnalysisException("Erro ao consultar o serviço de fraude", ex);
        }
    }


    private boolean shouldApprove(PolicyRequestEntity policy, RiskClassification classification) {
        BigDecimal value = policy.getInsuredAmount();
        String category = policy.getCategory().toUpperCase();

        return switch (classification) {
            case REGULAR -> switch (category) {
                case "VIDA", "RESIDENCIAL" -> value.compareTo(BigDecimal.valueOf(500_000)) <= 0;
                case "AUTO" -> value.compareTo(BigDecimal.valueOf(350_000)) <= 0;
                default -> value.compareTo(BigDecimal.valueOf(255_000)) <= 0;
            };
            case HIGH_RISK -> switch (category) {
                case "AUTO" -> value.compareTo(BigDecimal.valueOf(250_000)) <= 0;
                case "RESIDENCIAL" -> value.compareTo(BigDecimal.valueOf(150_000)) <= 0;
                default -> value.compareTo(BigDecimal.valueOf(125_000)) <= 0;
            };
            case PREFERENTIAL -> switch (category) {
                case "VIDA" -> value.compareTo(BigDecimal.valueOf(800_000)) <= 0;
                case "AUTO", "RESIDENCIAL" -> value.compareTo(BigDecimal.valueOf(450_000)) <= 0;
                default -> value.compareTo(BigDecimal.valueOf(375_000)) <= 0;
            };
            case NO_INFORMATION -> switch (category) {
                case "VIDA", "RESIDENCIAL" -> value.compareTo(BigDecimal.valueOf(200_000)) <= 0;
                case "AUTO" -> value.compareTo(BigDecimal.valueOf(75_000)) <= 0;
                default -> value.compareTo(BigDecimal.valueOf(55_000)) <= 0;
            };
        };
    }
}
