package com.itau.insurance.domain.model;

import com.itau.insurance.domain.enums.RequestStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyRequest {
    private UUID id;
    private UUID customerId;
    private UUID productId;
    private String category;
    private String salesChannel;
    private String paymentMethod;
    private BigDecimal totalMonthlyPremiumAmount;
    private BigDecimal insuredAmount;
    private Map<String, BigDecimal> coverages;
    private List<String> assistances;
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;
    private List<StatusHistory> history;
    private RequestStatus status;
}
