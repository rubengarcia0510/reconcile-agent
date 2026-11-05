package com.reconcile.agent.domain.model;

public record Anomaly(String id, String runId, AnomalyType type, String invoiceId,
                      String transactionId, String explanation, AnomalyStatus status) {}
