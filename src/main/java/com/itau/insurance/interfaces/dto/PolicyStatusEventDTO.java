package com.itau.insurance.interfaces.dto;

import com.itau.insurance.domain.enums.RequestStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PolicyStatusEventDTO {
    private UUID policyId;
    private UUID customerId;
    private RequestStatus status;
    private LocalDateTime timestamp;
}