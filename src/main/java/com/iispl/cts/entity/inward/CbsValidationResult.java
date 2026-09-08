package com.iispl.cts.entity.inward;

public class CbsValidationResult {

    private boolean passed;
    private String reason;

    public CbsValidationResult(boolean passed, String reason) {
        this.passed = passed;
        this.reason = reason;
    }

    public boolean isPassed() {
        return passed;
    }

    public String getReason() {
        return reason;
    }
}