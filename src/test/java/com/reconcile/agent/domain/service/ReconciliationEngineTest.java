package com.reconcile.agent.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.reconcile.agent.domain.model.BankTransaction;
import com.reconcile.agent.domain.model.Invoice;
import com.reconcile.agent.domain.model.MatchConfidence;
import com.reconcile.agent.domain.model.ReconciliationResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReconciliationEngineTest {

    private final ReconciliationEngine engine = ReconciliationEngine.withDefaults();

    private static Invoice invoice(String id, String number, String amount, String date) {
        return new Invoice(id, "run", "ACME", number, new BigDecimal(amount), "ARS", LocalDate.parse(date));
    }

    private static BankTransaction payment(String id, String reference, String amount, String date) {
        return new BankTransaction(id, "run", LocalDate.parse(date), "Transfer",
                new BigDecimal(amount), "ARS", reference);
    }

    @Test
    void matchesByInvoiceNumberWithHighConfidence() {
        ReconciliationResult result = engine.reconcile(
                List.of(invoice("i1", "0001-00045821", "184250.00", "2026-10-14")),
                List.of(payment("p1", "FC 0001-00045821", "-184250.00", "2026-10-16")));

        assertThat(result.matches()).hasSize(1);
        assertThat(result.matches().get(0).confidence()).isEqualTo(MatchConfidence.HIGH);
        assertThat(result.unmatchedInvoices()).isEmpty();
        assertThat(result.unmatchedTransactions()).isEmpty();
    }

    @Test
    void matchesByAmountAndDateWithMediumConfidence() {
        ReconciliationResult result = engine.reconcile(
                List.of(invoice("i1", "0001-00045821", "184250.00", "2026-10-14")),
                List.of(payment("p1", "", "-184250.00", "2026-10-14")));

        assertThat(result.matches()).hasSize(1);
        assertThat(result.matches().get(0).confidence()).isEqualTo(MatchConfidence.MEDIUM);
    }

    @Test
    void leavesBothUnmatchedWhenAmountDiffers() {
        ReconciliationResult result = engine.reconcile(
                List.of(invoice("i1", "0001-00045821", "184250.00", "2026-10-14")),
                List.of(payment("p1", "", "-100000.00", "2026-10-14")));

        assertThat(result.matches()).isEmpty();
        assertThat(result.unmatchedInvoices()).hasSize(1);
        assertThat(result.unmatchedTransactions()).hasSize(1);
    }

    @Test
    void doesNotMatchOutsideDateTolerance() {
        ReconciliationResult result = engine.reconcile(
                List.of(invoice("i1", "0001-00045821", "184250.00", "2026-10-14")),
                List.of(payment("p1", "", "-184250.00", "2026-11-30")));

        assertThat(result.matches()).isEmpty();
    }

    @Test
    void duplicateInvoicesShareASinglePayment() {
        ReconciliationResult result = engine.reconcile(
                List.of(invoice("i1", "0001-00045821", "184250.00", "2026-10-14"),
                        invoice("i2", "0001-00045821", "184250.00", "2026-10-14")),
                List.of(payment("p1", "FC 0001-00045821", "-184250.00", "2026-10-15")));

        assertThat(result.matches()).hasSize(1);
        assertThat(result.unmatchedInvoices()).hasSize(1);
    }
}
