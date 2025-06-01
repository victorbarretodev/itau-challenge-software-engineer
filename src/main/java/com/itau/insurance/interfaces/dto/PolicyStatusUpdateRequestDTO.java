package com.itau.insurance.interfaces.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class PolicyStatusUpdateRequestDTO {
    private UUID policyId;
    private boolean paymentConfirmed;
    private boolean subscriptionAuthorized;
}
