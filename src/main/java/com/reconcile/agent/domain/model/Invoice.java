package com.reconcile.agent.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Invoice(String id, String supplier, String number, BigDecimal amount,
                      String currency, LocalDate issueDate) {}
