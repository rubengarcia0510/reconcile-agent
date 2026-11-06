package com.reconcile.agent.domain.model;

import java.util.List;

public record RunSummary(String runId, int invoicesProcessed, int invoicesFailed,
                         long transactionsLoaded, List<String> failedFiles) {}
