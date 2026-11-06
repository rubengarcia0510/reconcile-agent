package com.reconcile.agent.domain.service;

import com.reconcile.agent.domain.model.BankTransaction;
import com.reconcile.agent.domain.model.Invoice;
import com.reconcile.agent.domain.model.Match;
import com.reconcile.agent.domain.model.MatchConfidence;
import com.reconcile.agent.domain.model.ReconciliationResult;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public final class ReconciliationEngine {

    private final int dateToleranceDays;
    private final BigDecimal amountTolerance;

    public ReconciliationEngine(int dateToleranceDays, BigDecimal amountTolerance) {
        this.dateToleranceDays = dateToleranceDays;
        this.amountTolerance = amountTolerance;
    }

    public static ReconciliationEngine withDefaults() {
        return new ReconciliationEngine(5, BigDecimal.ZERO);
    }

    public ReconciliationResult reconcile(List<Invoice> invoices, List<BankTransaction> transactions) {
        List<Invoice> pendingInvoices = new ArrayList<>(invoices);
        List<BankTransaction> pendingTransactions = new ArrayList<>(transactions);
        List<Match> matches = new ArrayList<>();

        matchPass(pendingInvoices, pendingTransactions, matches, true);
        matchPass(pendingInvoices, pendingTransactions, matches, false);

        return new ReconciliationResult(List.copyOf(matches), List.copyOf(pendingInvoices),
                List.copyOf(pendingTransactions));
    }

    private void matchPass(List<Invoice> invoices, List<BankTransaction> transactions,
                           List<Match> matches, boolean requireInvoiceNumber) {
        Iterator<Invoice> iterator = invoices.iterator();
        while (iterator.hasNext()) {
            Invoice invoice = iterator.next();
            Optional<BankTransaction> candidate = transactions.stream()
                    .filter(t -> compatible(invoice, t))
                    .filter(t -> !requireInvoiceNumber || mentionsNumber(invoice, t))
                    .min(Comparator.comparingLong(t -> daysBetween(invoice, t)));
            if (candidate.isPresent()) {
                transactions.remove(candidate.get());
                matches.add(new Match(invoice, candidate.get(),
                        requireInvoiceNumber ? MatchConfidence.HIGH : MatchConfidence.MEDIUM));
                iterator.remove();
            }
        }
    }

    private boolean compatible(Invoice invoice, BankTransaction transaction) {
        if (invoice.amount() == null || transaction.amount() == null
                || invoice.issueDate() == null || transaction.date() == null) {
            return false;
        }
        if (invoice.currency() != null && transaction.currency() != null
                && !invoice.currency().equalsIgnoreCase(transaction.currency())) {
            return false;
        }
        BigDecimal difference = transaction.amount().abs().subtract(invoice.amount().abs()).abs();
        BigDecimal allowed = invoice.amount().abs().multiply(amountTolerance);
        return difference.compareTo(allowed) <= 0 && daysBetween(invoice, transaction) <= dateToleranceDays;
    }

    private boolean mentionsNumber(Invoice invoice, BankTransaction transaction) {
        String number = normalize(invoice.number());
        if (number.length() < 4) {
            return false;
        }
        return normalize(transaction.reference()).contains(number)
                || normalize(transaction.description()).contains(number);
    }

    private long daysBetween(Invoice invoice, BankTransaction transaction) {
        return Math.abs(ChronoUnit.DAYS.between(invoice.issueDate(), transaction.date()));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
    }
}
