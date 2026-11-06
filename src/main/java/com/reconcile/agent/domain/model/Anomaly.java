package com.reconcile.agent.domain.model;

public record Anomaly(String id, String runId, AnomalyType type, String invoiceId,
                      String transactionId, String explanation, AnomalyStatus status) {

    public Anomaly withStatus(AnomalyStatus newStatus) {
        return new Anomaly(id, runId, type, invoiceId, transactionId, explanation, newStatus);
    }
}
