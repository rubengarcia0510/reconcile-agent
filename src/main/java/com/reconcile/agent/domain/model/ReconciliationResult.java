package com.reconcile.agent.domain.model;

import java.util.List;

public record ReconciliationResult(List<Match> matches,
                                   List<Invoice> unmatchedInvoices,
                                   List<BankTransaction> unmatchedTransactions) {}
