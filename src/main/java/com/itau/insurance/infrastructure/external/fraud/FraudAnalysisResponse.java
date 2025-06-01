package com.itau.insurance.infrastructure.external.fraud;

import com.itau.insurance.domain.enums.RiskClassification;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudAnalysisResponse {

    private UUID orderId;
    private UUID customerId;
    private LocalDateTime analyzedAt;
    private RiskClassification classification;
    private List<FraudOccurrence> occurrences;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FraudOccurrence {
        private UUID id;
        private long productId;
        private String type;
        private String description;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
