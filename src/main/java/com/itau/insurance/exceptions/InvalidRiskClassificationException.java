package com.itau.insurance.exceptions;

public class InvalidRiskClassificationException extends RuntimeException {
    public InvalidRiskClassificationException(String message) {
        super(message);
    }
}
