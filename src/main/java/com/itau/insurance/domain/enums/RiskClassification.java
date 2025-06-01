package com.itau.insurance.domain.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum RiskClassification {
    REGULAR,
    HIGH_RISK,
    PREFERENTIAL,
    NO_INFORMATION
}
