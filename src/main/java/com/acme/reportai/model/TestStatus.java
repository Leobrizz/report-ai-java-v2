package com.acme.reportai.model;

public enum TestStatus {
    PASSED,
    FAILED,
    BROKEN,
    SKIPPED,
    UNKNOWN;

    public boolean isFailure() {
        return this == FAILED || this == BROKEN;
    }
}
