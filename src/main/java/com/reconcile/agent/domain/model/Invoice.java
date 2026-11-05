package com.reconcile.agent.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Invoice(String id, String runId, String supplier, String number,
                      BigDecimal amount, String currency, LocalDate issueDate) {

    public Invoice withRunId(String runId) {
        return new Invoice(id, runId, supplier, number, amount, currency, issueDate);
    }
}
