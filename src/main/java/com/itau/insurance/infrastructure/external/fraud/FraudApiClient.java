package com.itau.insurance.infrastructure.external.fraud;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FraudApiClient {

    private final WebClient webClient;

    @Value("${external.fraud-api.base-url}")
    private String fraudApiBaseUrl;

    public FraudAnalysisResponse analyze(UUID policyId) {
        return webClient.get()
                .uri(fraudApiBaseUrl + "/fraud/" + policyId)
                .retrieve()
                .bodyToMono(FraudAnalysisResponse.class)
                .block(); // Bloco pois não estamos usando WebFlux
    }
}
