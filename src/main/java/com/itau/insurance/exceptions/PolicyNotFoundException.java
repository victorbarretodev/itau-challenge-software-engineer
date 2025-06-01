package com.itau.insurance.exceptions;

import java.util.UUID;

public class PolicyNotFoundException extends RuntimeException {
    public PolicyNotFoundException(UUID id) {
        super("Apólice com ID " + id + " não encontrada.");
    }

    public PolicyNotFoundException(String message) {
        super(message);
    }
}
