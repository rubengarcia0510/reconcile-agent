package com.reconcile.agent.application;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
