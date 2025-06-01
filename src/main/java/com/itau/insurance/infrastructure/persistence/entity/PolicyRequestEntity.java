package com.itau.insurance.infrastructure.persistence.entity;

import com.itau.insurance.domain.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "policy_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyRequestEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String salesChannel;

    @Column(nullable = false)
    private String paymentMethod;

    @Column(nullable = false)
    private BigDecimal totalMonthlyPremiumAmount;

    @Column(nullable = false)
    private BigDecimal insuredAmount;

    @ElementCollection
    @CollectionTable(name = "policy_coverages", joinColumns = @JoinColumn(name = "policy_request_id"))
    @MapKeyColumn(name = "coverage_type")
    @Column(name = "coverage_value")
    private Map<String, BigDecimal> coverages = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "policy_assistances", joinColumns = @JoinColumn(name = "policy_request_id"))
    @Column(name = "assistance")
    private List<String> assistances = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime finishedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "policy_request_id")
    private List<StatusHistoryEntity> history = new ArrayList<>();
}
