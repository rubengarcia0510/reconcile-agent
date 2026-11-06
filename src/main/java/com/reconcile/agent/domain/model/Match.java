package com.reconcile.agent.domain.model;

public record Match(Invoice invoice, BankTransaction transaction, MatchConfidence confidence) {}
