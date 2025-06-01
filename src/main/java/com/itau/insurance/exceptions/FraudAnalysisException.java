package com.itau.insurance.exceptions;

public class FraudAnalysisException extends BusinessException {
    public FraudAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
