package com.itau.insurance.exceptions;

public class PolicyCreationException extends RuntimeException {
    public PolicyCreationException(String message) {
        super(message);
    }

    public PolicyCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
