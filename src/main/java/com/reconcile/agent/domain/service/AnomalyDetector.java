package com.reconcile.agent.domain.service;

import com.reconcile.agent.domain.model.Anomaly;
import com.reconcile.agent.domain.model.AnomalyStatus;
import com.reconcile.agent.domain.model.AnomalyType;
import com.reconcile.agent.domain.model.BankTransaction;
import com.reconcile.agent.domain.model.Invoice;
import com.reconcile.agent.domain.model.ReconciliationResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class AnomalyDetector {

    private static final int MIN_INVOICES_FOR_MEDIAN = 3;

    private final int dateToleranceDays;
    private final BigDecimal mismatchTolerance;
    private final BigDecimal unusualMultiplier;

    public AnomalyDetector(int dateToleranceDays, BigDecimal mismatchTolerance, BigDecimal unusualMultiplier) {
        this.dateToleranceDays = dateToleranceDays;
        this.mismatchTolerance = mismatchTolerance;
        this.unusualMultiplier = unusualMultiplier;
    }

    public static AnomalyDetector withDefaults() {
        return new AnomalyDetector(5, new BigDecimal("0.10"), new BigDecimal("3"));
    }

    public List<Anomaly> detect(List<Invoice> invoices, ReconciliationResult reconciliation) {
        List<Anomaly> anomalies = new ArrayList<>();
        anomalies.addAll(duplicates(invoices));
        anomalies.addAll(unusualAmounts(invoices));

        List<BankTransaction> freePayments = new ArrayList<>(reconciliation.unmatchedTransactions().stream()
                .filter(t -> t.amount() != null && t.amount().signum() < 0)
                .toList());
        anomalies.addAll(mismatches(reconciliation.unmatchedInvoices(), freePayments));

        for (BankTransaction payment : freePayments) {
            anomalies.add(new Anomaly(null, payment.runId(), AnomalyType.PAYMENT_WITHOUT_INVOICE, null,
                    payment.id(),
                    "Payment of %s %s on %s has no matching invoice".formatted(
                            payment.amount().abs().toPlainString(), nullToDash(payment.currency()), payment.date()),
                    AnomalyStatus.PENDING));
        }
        return List.copyOf(anomalies);
    }

    private List<Anomaly> duplicates(List<Invoice> invoices) {
        Map<String, List<Invoice>> groups = new LinkedHashMap<>();
        for (Invoice invoice : invoices) {
            if (invoice.number() == null || invoice.amount() == null) {
                continue;
            }
            String key = normalize(invoice.supplier()) + "|" + normalize(invoice.number()) + "|"
                    + invoice.amount().stripTrailingZeros().toPlainString();
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(invoice);
        }
        List<Anomaly> result = new ArrayList<>();
        for (List<Invoice> group : groups.values()) {
            for (int n = 1; n < group.size(); n++) {
                Invoice duplicate = group.get(n);
                result.add(new Anomaly(null, duplicate.runId(), AnomalyType.DUPLICATE_INVOICE, duplicate.id(), null,
                        "Invoice %s from %s appears more than once with the same amount (%s %s)".formatted(
                                duplicate.number(), nullToDash(duplicate.supplier()),
                                duplicate.amount().toPlainString(), nullToDash(duplicate.currency())),
                        AnomalyStatus.PENDING));
            }
        }
        return result;
    }

    private List<Anomaly> unusualAmounts(List<Invoice> invoices) {
        List<BigDecimal> amounts = invoices.stream()
                .filter(i -> i.amount() != null)
                .map(i -> i.amount().abs())
                .sorted()
                .toList();
        if (amounts.size() < MIN_INVOICES_FOR_MEDIAN) {
            return List.of();
        }
        BigDecimal median = median(amounts);
        if (median.signum() == 0) {
            return List.of();
        }
        BigDecimal threshold = median.multiply(unusualMultiplier);
        List<Anomaly> result = new ArrayList<>();
        for (Invoice invoice : invoices) {
            if (invoice.amount() != null && invoice.amount().abs().compareTo(threshold) > 0) {
                result.add(new Anomaly(null, invoice.runId(), AnomalyType.UNUSUAL_AMOUNT, invoice.id(), null,
                        "Invoice %s amount %s %s is far above the typical amount of this batch (median %s)".formatted(
                                invoice.number(), invoice.amount().toPlainString(), nullToDash(invoice.currency()),
                                median.toPlainString()),
                        AnomalyStatus.PENDING));
            }
        }
        return result;
    }

    private List<Anomaly> mismatches(List<Invoice> unmatchedInvoices, List<BankTransaction> freePayments) {
        List<Anomaly> result = new ArrayList<>();
        for (Invoice invoice : unmatchedInvoices) {
            if (invoice.amount() == null || invoice.issueDate() == null) {
                continue;
            }
            BigDecimal allowed = invoice.amount().abs().multiply(mismatchTolerance);
            Optional<BankTransaction> closest = freePayments.stream()
                    .filter(t -> t.date() != null)
                    .filter(t -> sameCurrency(invoice, t))
                    .filter(t -> Math.abs(ChronoUnit.DAYS.between(invoice.issueDate(), t.date())) <= dateToleranceDays)
                    .filter(t -> difference(invoice, t).compareTo(allowed) <= 0)
                    .min(Comparator.comparing(t -> difference(invoice, t)));
            if (closest.isPresent()) {
                BankTransaction payment = closest.get();
                freePayments.remove(payment);
                result.add(new Anomaly(null, invoice.runId(), AnomalyType.AMOUNT_MISMATCH, invoice.id(),
                        payment.id(),
                        "Invoice %s is for %s %s but the closest payment is %s (difference %s)".formatted(
                                invoice.number(), invoice.amount().toPlainString(), nullToDash(invoice.currency()),
                                payment.amount().abs().toPlainString(), difference(invoice, payment).toPlainString()),
                        AnomalyStatus.PENDING));
            }
        }
        return result;
    }

    private static BigDecimal difference(Invoice invoice, BankTransaction payment) {
        return payment.amount().abs().subtract(invoice.amount().abs()).abs();
    }

    private static boolean sameCurrency(Invoice invoice, BankTransaction payment) {
        return invoice.currency() == null || payment.currency() == null
                || invoice.currency().equalsIgnoreCase(payment.currency());
    }

    private static BigDecimal median(List<BigDecimal> sorted) {
        int n = sorted.size();
        if (n % 2 == 1) {
            return sorted.get(n / 2);
        }
        return sorted.get(n / 2 - 1).add(sorted.get(n / 2)).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
    }

    private static String nullToDash(String value) {
        return value == null ? "-" : value;
    }
}
