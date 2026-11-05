package com.reconcile.agent.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BankTransaction(String id, String runId, LocalDate date, String description,
                              BigDecimal amount, String currency, String reference) {}
