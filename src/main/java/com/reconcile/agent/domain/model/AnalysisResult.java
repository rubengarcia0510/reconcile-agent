package com.reconcile.agent.domain.model;

import java.util.List;

public record AnalysisResult(String runId, int matched, int unmatchedInvoices,
                             int unmatchedTransactions, List<Anomaly> anomalies) {}
