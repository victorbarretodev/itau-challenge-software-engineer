package com.itau.insurance.interfaces.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class PolicyStatusEventRequestDTO {
    private UUID id;
    private boolean paymentConfirmed;
    private boolean subscriptionAuthorized;
}
