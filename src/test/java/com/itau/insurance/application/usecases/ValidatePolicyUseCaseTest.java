package com.itau.insurance.application.usecases;

import com.itau.insurance.domain.enums.RequestStatus;
import com.itau.insurance.domain.enums.RiskClassification;
import com.itau.insurance.infrastructure.external.fraud.FraudApiClient;
import com.itau.insurance.infrastructure.external.fraud.FraudAnalysisResponse;
import com.itau.insurance.infrastructure.messaging.EventPublisher;
import com.itau.insurance.infrastructure.persistence.entity.PolicyRequestEntity;
import com.itau.insurance.infrastructure.persistence.repository.PolicyRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidatePolicyUseCaseTest {

    private FraudApiClient fraudApiClient;
    private PolicyRequestRepository repository;
    private EventPublisher eventPublisher;
    private ValidatePolicyUseCase useCase;

    @BeforeEach
    void setUp() {
        fraudApiClient = mock(FraudApiClient.class);
        repository = mock(PolicyRequestRepository.class);
        eventPublisher = mock(EventPublisher.class);
        useCase = new ValidatePolicyUseCase(fraudApiClient, repository, eventPublisher);
    }

    @Test
    void deveValidarSolicitacaoQuandoClassificacaoPermitir() {
        UUID policyId = UUID.randomUUID();
        PolicyRequestEntity policy = PolicyRequestEntity.builder()
                .id(policyId)
                .category("VIDA")
                .insuredAmount(BigDecimal.valueOf(300_000))
                .history(new ArrayList<>())
                .build();

        FraudAnalysisResponse response = FraudAnalysisResponse.builder()
                .classification(RiskClassification.REGULAR)
                .build();

        when(repository.findById(policyId)).thenReturn(Optional.of(policy));
        when(fraudApiClient.analyze(policyId)).thenReturn(response);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyRequestEntity result = useCase.execute(policyId);

        assertEquals(RequestStatus.VALIDATED, result.getStatus());
        verify(eventPublisher).publishPolicyEvent(result);
        verify(repository).save(result);
    }

    @Test
    void deveRejeitarSolicitacaoQuandoClassificacaoNaoPermitir() {
        UUID policyId = UUID.randomUUID();
        PolicyRequestEntity policy = PolicyRequestEntity.builder()
                .id(policyId)
                .category("AUTO")
                .insuredAmount(BigDecimal.valueOf(1_000_000)) // alto demais
                .history(new ArrayList<>())
                .build();

        FraudAnalysisResponse response = FraudAnalysisResponse.builder()
                .classification(RiskClassification.HIGH_RISK)
                .build();

        when(repository.findById(policyId)).thenReturn(Optional.of(policy));
        when(fraudApiClient.analyze(policyId)).thenReturn(response);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyRequestEntity result = useCase.execute(policyId);

        assertEquals(RequestStatus.REJECTED, result.getStatus());
        verify(eventPublisher).publishPolicyEvent(result);
        verify(repository).save(result);
    }

    @Test
    void deveValidarClassificacaoRegularComCategoriaDesconhecidaAbaixoLimite() {
        UUID policyId = UUID.randomUUID();
        PolicyRequestEntity policy = PolicyRequestEntity.builder()
                .id(policyId)
                .category("EMPRESARIAL")
                .insuredAmount(BigDecimal.valueOf(200_000)) // < 255_000
                .history(new ArrayList<>())
                .build();

        FraudAnalysisResponse response = FraudAnalysisResponse.builder()
                .classification(RiskClassification.REGULAR)
                .build();

        when(repository.findById(policyId)).thenReturn(Optional.of(policy));
        when(fraudApiClient.analyze(policyId)).thenReturn(response);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyRequestEntity result = useCase.execute(policyId);

        assertEquals(RequestStatus.VALIDATED, result.getStatus());
    }

    @Test
    void deveValidarPreferentialCategoriaVidaDentroLimite() {
        UUID policyId = UUID.randomUUID();
        PolicyRequestEntity policy = PolicyRequestEntity.builder()
                .id(policyId)
                .category("VIDA")
                .insuredAmount(BigDecimal.valueOf(750_000)) // dentro
                .history(new ArrayList<>())
                .build();

        FraudAnalysisResponse response = FraudAnalysisResponse.builder()
                .classification(RiskClassification.PREFERENTIAL)
                .build();

        when(repository.findById(policyId)).thenReturn(Optional.of(policy));
        when(fraudApiClient.analyze(policyId)).thenReturn(response);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyRequestEntity result = useCase.execute(policyId);

        assertEquals(RequestStatus.VALIDATED, result.getStatus());
    }

    @Test
    void deveRejeitarNoInformationCategoriaAutoForaLimite() {
        UUID policyId = UUID.randomUUID();
        PolicyRequestEntity policy = PolicyRequestEntity.builder()
                .id(policyId)
                .category("AUTO")
                .insuredAmount(BigDecimal.valueOf(100_000)) // > 75_000
                .history(new ArrayList<>())
                .build();

        FraudAnalysisResponse response = FraudAnalysisResponse.builder()
                .classification(RiskClassification.NO_INFORMATION)
                .build();

        when(repository.findById(policyId)).thenReturn(Optional.of(policy));
        when(fraudApiClient.analyze(policyId)).thenReturn(response);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyRequestEntity result = useCase.execute(policyId);

        assertEquals(RequestStatus.REJECTED, result.getStatus());
    }

    @Test
    void deveLancarExcecaoQuandoClassificacaoNaoForReconhecida() {
        UUID policyId = UUID.randomUUID();
        PolicyRequestEntity policy = PolicyRequestEntity.builder()
                .id(policyId)
                .category("AUTO")
                .insuredAmount(BigDecimal.valueOf(50_000))
                .history(new ArrayList<>())
                .build();

        FraudAnalysisResponse response = FraudAnalysisResponse.builder()
                .classification(null) // força quebra de lógica
                .build();

        when(repository.findById(policyId)).thenReturn(Optional.of(policy));
        when(fraudApiClient.analyze(policyId)).thenReturn(response);

        assertThrows(RuntimeException.class, () -> useCase.execute(policyId));
    }

    @Test
    void deveValidarHighRiskCategoriaAutoDentroDoLimite() {
        UUID policyId = UUID.randomUUID();
        PolicyRequestEntity policy = PolicyRequestEntity.builder()
                .id(policyId)
                .category("AUTO")
                .insuredAmount(BigDecimal.valueOf(240_000)) // <= 250_000
                .history(new ArrayList<>())
                .build();

        FraudAnalysisResponse response = FraudAnalysisResponse.builder()
                .classification(RiskClassification.HIGH_RISK)
                .build();

        when(repository.findById(policyId)).thenReturn(Optional.of(policy));
        when(fraudApiClient.analyze(policyId)).thenReturn(response);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyRequestEntity result = useCase.execute(policyId);
        assertEquals(RequestStatus.VALIDATED, result.getStatus());
    }

    @Test
    void deveRejeitarHighRiskCategoriaResidencialForaDoLimite() {
        UUID policyId = UUID.randomUUID();
        PolicyRequestEntity policy = PolicyRequestEntity.builder()
                .id(policyId)
                .category("RESIDENCIAL")
                .insuredAmount(BigDecimal.valueOf(200_000)) // > 150_000
                .history(new ArrayList<>())
                .build();

        FraudAnalysisResponse response = FraudAnalysisResponse.builder()
                .classification(RiskClassification.HIGH_RISK)
                .build();

        when(repository.findById(policyId)).thenReturn(Optional.of(policy));
        when(fraudApiClient.analyze(policyId)).thenReturn(response);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyRequestEntity result = useCase.execute(policyId);
        assertEquals(RequestStatus.REJECTED, result.getStatus());
    }

    @Test
    void deveValidarPreferentialCategoriaAutoDentroLimite() {
        UUID policyId = UUID.randomUUID();
        PolicyRequestEntity policy = PolicyRequestEntity.builder()
                .id(policyId)
                .category("AUTO")
                .insuredAmount(BigDecimal.valueOf(400_000)) // <= 450_000
                .history(new ArrayList<>())
                .build();

        FraudAnalysisResponse response = FraudAnalysisResponse.builder()
                .classification(RiskClassification.PREFERENTIAL)
                .build();

        when(repository.findById(policyId)).thenReturn(Optional.of(policy));
        when(fraudApiClient.analyze(policyId)).thenReturn(response);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyRequestEntity result = useCase.execute(policyId);
        assertEquals(RequestStatus.VALIDATED, result.getStatus());
    }

    @Test
    void deveRejeitarNoInformationCategoriaDesconhecidaForaDoLimite() {
        UUID policyId = UUID.randomUUID();
        PolicyRequestEntity policy = PolicyRequestEntity.builder()
                .id(policyId)
                .category("EMPRESARIAL")
                .insuredAmount(BigDecimal.valueOf(100_000)) // > 55_000
                .history(new ArrayList<>())
                .build();

        FraudAnalysisResponse response = FraudAnalysisResponse.builder()
                .classification(RiskClassification.NO_INFORMATION)
                .build();

        when(repository.findById(policyId)).thenReturn(Optional.of(policy));
        when(fraudApiClient.analyze(policyId)).thenReturn(response);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyRequestEntity result = useCase.execute(policyId);
        assertEquals(RequestStatus.REJECTED, result.getStatus());
    }

    @Test
    void deveValidarNoInformationCategoriaAutoDentroLimite() {
        UUID policyId = UUID.randomUUID();
        PolicyRequestEntity policy = PolicyRequestEntity.builder()
                .id(policyId)
                .category("AUTO")
                .insuredAmount(BigDecimal.valueOf(50_000)) // <= 75_000
                .history(new ArrayList<>())
                .build();

        FraudAnalysisResponse response = FraudAnalysisResponse.builder()
                .classification(RiskClassification.NO_INFORMATION)
                .build();

        when(repository.findById(policyId)).thenReturn(Optional.of(policy));
        when(fraudApiClient.analyze(policyId)).thenReturn(response);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyRequestEntity result = useCase.execute(policyId);
        assertEquals(RequestStatus.VALIDATED, result.getStatus());
    }

    @Test
    void deveValidarPreferentialCategoriaDesconhecidaDentroDoLimite() {
        UUID policyId = UUID.randomUUID();
        PolicyRequestEntity policy = PolicyRequestEntity.builder()
                .id(policyId)
                .category("EMPRESARIAL")
                .insuredAmount(BigDecimal.valueOf(300_000)) // <= 375_000
                .history(new ArrayList<>())
                .build();

        FraudAnalysisResponse response = FraudAnalysisResponse.builder()
                .classification(RiskClassification.PREFERENTIAL)
                .build();

        when(repository.findById(policyId)).thenReturn(Optional.of(policy));
        when(fraudApiClient.analyze(policyId)).thenReturn(response);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyRequestEntity result = useCase.execute(policyId);
        assertEquals(RequestStatus.VALIDATED, result.getStatus());
    }

    @Test
    void deveValidarNoInformationCategoriaVidaDentroLimite() {
        UUID policyId = UUID.randomUUID();
        PolicyRequestEntity policy = PolicyRequestEntity.builder()
                .id(policyId)
                .category("VIDA")
                .insuredAmount(BigDecimal.valueOf(180_000)) // <= 200_000
                .history(new ArrayList<>())
                .build();

        FraudAnalysisResponse response = FraudAnalysisResponse.builder()
                .classification(RiskClassification.NO_INFORMATION)
                .build();

        when(repository.findById(policyId)).thenReturn(Optional.of(policy));
        when(fraudApiClient.analyze(policyId)).thenReturn(response);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyRequestEntity result = useCase.execute(policyId);
        assertEquals(RequestStatus.VALIDATED, result.getStatus());
    }

    @Test
    void deveValidarPreferentialCategoriaResidencialDentroDoLimite() {
        UUID policyId = UUID.randomUUID();
        PolicyRequestEntity policy = PolicyRequestEntity.builder()
                .id(policyId)
                .category("RESIDENCIAL")
                .insuredAmount(BigDecimal.valueOf(400_000)) // <= 450_000
                .history(new ArrayList<>())
                .build();

        FraudAnalysisResponse response = FraudAnalysisResponse.builder()
                .classification(RiskClassification.PREFERENTIAL)
                .build();

        when(repository.findById(policyId)).thenReturn(Optional.of(policy));
        when(fraudApiClient.analyze(policyId)).thenReturn(response);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyRequestEntity result = useCase.execute(policyId);
        assertEquals(RequestStatus.VALIDATED, result.getStatus());
    }

    @Test
    void deveValidarRegularCategoriaAutoDentroDoLimite() {
        UUID policyId = UUID.randomUUID();
        PolicyRequestEntity policy = PolicyRequestEntity.builder()
                .id(policyId)
                .category("AUTO")
                .insuredAmount(BigDecimal.valueOf(340_000)) // <= 350.000
                .history(new ArrayList<>())
                .build();

        FraudAnalysisResponse response = FraudAnalysisResponse.builder()
                .classification(RiskClassification.REGULAR)
                .build();

        when(repository.findById(policyId)).thenReturn(Optional.of(policy));
        when(fraudApiClient.analyze(policyId)).thenReturn(response);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyRequestEntity result = useCase.execute(policyId);

        assertEquals(RequestStatus.VALIDATED, result.getStatus());
    }



}
