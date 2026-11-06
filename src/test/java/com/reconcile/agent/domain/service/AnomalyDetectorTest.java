package com.reconcile.agent.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.reconcile.agent.domain.model.Anomaly;
import com.reconcile.agent.domain.model.AnomalyType;
import com.reconcile.agent.domain.model.BankTransaction;
import com.reconcile.agent.domain.model.Invoice;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class AnomalyDetectorTest {

    private final ReconciliationEngine engine = ReconciliationEngine.withDefaults();
    private final AnomalyDetector detector = AnomalyDetector.withDefaults();

    private static Invoice invoice(String id, String number, String amount, String date) {
        return new Invoice(id, "run", "ACME", number, new BigDecimal(amount), "ARS", LocalDate.parse(date));
    }

    private static BankTransaction tx(String id, String amount, String date) {
        return new BankTransaction(id, "run", LocalDate.parse(date), "Transfer",
                new BigDecimal(amount), "ARS", "");
    }

    private List<Anomaly> analyze(List<Invoice> invoices, List<BankTransaction> transactions) {
        return detector.detect(invoices, engine.reconcile(invoices, transactions));
    }

    @Test
    void cleanBatchHasNoAnomalies() {
        List<Anomaly> anomalies = analyze(
                List.of(invoice("i1", "0001-00045821", "184250.00", "2026-10-14")),
                List.of(tx("p1", "-184250.00", "2026-10-14")));

        assertThat(anomalies).isEmpty();
    }

    @Test
    void flagsTheSecondCopyOfADuplicatedInvoice() {
        List<Anomaly> anomalies = analyze(
                List.of(invoice("i1", "0001-00045821", "184250.00", "2026-10-14"),
                        invoice("i2", "0001-00045821", "184250.00", "2026-10-14")),
                List.of(tx("p1", "-184250.00", "2026-10-14")));

        assertThat(anomalies).hasSize(1);
        assertThat(anomalies.get(0).type()).isEqualTo(AnomalyType.DUPLICATE_INVOICE);
        assertThat(anomalies.get(0).invoiceId()).isEqualTo("i2");
    }

    @Test
    void flagsAnAmountFarAboveTheMedian() {
        List<Anomaly> anomalies = analyze(
                List.of(invoice("i1", "A-0001", "100000.00", "2026-10-01"),
                        invoice("i2", "A-0002", "120000.00", "2026-10-02"),
                        invoice("i3", "A-0003", "110000.00", "2026-10-03"),
                        invoice("i4", "A-0004", "5000000.00", "2026-10-04")),
                List.of());

        assertThat(anomalies).hasSize(1);
        assertThat(anomalies.get(0).type()).isEqualTo(AnomalyType.UNUSUAL_AMOUNT);
        assertThat(anomalies.get(0).invoiceId()).isEqualTo("i4");
    }

    @Test
    void flagsAPaymentWithoutInvoice() {
        List<Anomaly> anomalies = analyze(List.of(), List.of(tx("p1", "-50000.00", "2026-10-10")));

        assertThat(anomalies).hasSize(1);
        assertThat(anomalies.get(0).type()).isEqualTo(AnomalyType.PAYMENT_WITHOUT_INVOICE);
        assertThat(anomalies.get(0).transactionId()).isEqualTo("p1");
    }

    @Test
    void ignoresIncomingMoney() {
        List<Anomaly> anomalies = analyze(List.of(), List.of(tx("p1", "50000.00", "2026-10-10")));

        assertThat(anomalies).isEmpty();
    }

    @Test
    void flagsAClosePaymentWithADifferentAmountAsMismatch() {
        List<Anomaly> anomalies = analyze(
                List.of(invoice("i1", "0001-00045821", "184250.00", "2026-10-14")),
                List.of(tx("p1", "-180000.00", "2026-10-16")));

        assertThat(anomalies).hasSize(1);
        assertThat(anomalies.get(0).type()).isEqualTo(AnomalyType.AMOUNT_MISMATCH);
        assertThat(anomalies.get(0).invoiceId()).isEqualTo("i1");
        assertThat(anomalies.get(0).transactionId()).isEqualTo("p1");
    }
}
