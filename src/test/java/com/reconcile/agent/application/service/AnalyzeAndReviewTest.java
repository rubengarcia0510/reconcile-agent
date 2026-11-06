package com.reconcile.agent.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.reconcile.agent.application.ConflictException;
import com.reconcile.agent.application.NotFoundException;
import com.reconcile.agent.application.port.out.AnomalyRepository;
import com.reconcile.agent.application.port.out.BankTransactionRepository;
import com.reconcile.agent.application.port.out.InvoiceRepository;
import com.reconcile.agent.domain.model.AnalysisResult;
import com.reconcile.agent.domain.model.Anomaly;
import com.reconcile.agent.domain.model.AnomalyStatus;
import com.reconcile.agent.domain.model.AnomalyType;
import com.reconcile.agent.domain.model.BankTransaction;
import com.reconcile.agent.domain.model.Invoice;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class AnalyzeAndReviewTest {

    private final FakeInvoices invoices = new FakeInvoices();
    private final FakeTransactions transactions = new FakeTransactions();
    private final FakeAnomalies anomalies = new FakeAnomalies();
    private final AnalyzeRunService analyze = new AnalyzeRunService(invoices, transactions, anomalies);
    private final ReviewAnomalyService review = new ReviewAnomalyService(anomalies);

    private void loadScenario() {
        invoices.items.add(new Invoice("i1", "run", "ACME", "0001-00000001", new BigDecimal("100000.00"),
                "ARS", LocalDate.parse("2026-10-01")));
        invoices.items.add(new Invoice("i2", "run", "ACME", "0001-00000001", new BigDecimal("100000.00"),
                "ARS", LocalDate.parse("2026-10-01")));
        transactions.items.add(new BankTransaction("p1", "run", LocalDate.parse("2026-10-02"), "Transfer",
                new BigDecimal("-100000.00"), "ARS", "FC 0001-00000001"));
        transactions.items.add(new BankTransaction("p2", "run", LocalDate.parse("2026-10-05"), "Unknown vendor",
                new BigDecimal("-75000.00"), "ARS", ""));
    }

    @Test
    void analysisStoresTheDetectedAnomalies() {
        loadScenario();

        AnalysisResult result = analyze.analyze("run").block();

        assertThat(result.matched()).isEqualTo(1);
        assertThat(result.anomalies()).extracting(Anomaly::type)
                .containsExactlyInAnyOrder(AnomalyType.DUPLICATE_INVOICE, AnomalyType.PAYMENT_WITHOUT_INVOICE);
        assertThat(anomalies.items).hasSize(2).allMatch(a -> a.status() == AnomalyStatus.PENDING);
    }

    @Test
    void analyzingTwiceDoesNotDuplicateAnomalies() {
        loadScenario();

        analyze.analyze("run").block();
        analyze.analyze("run").block();

        assertThat(anomalies.items).hasSize(2);
    }

    @Test
    void analyzingAnUnknownRunFails() {
        assertThatThrownBy(() -> analyze.analyze("missing").block())
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void approvingChangesTheStatusAndCannotBeRepeated() {
        loadScenario();
        AnalysisResult result = analyze.analyze("run").block();
        String id = result.anomalies().get(0).id();

        Anomaly approved = review.approve(id).block();

        assertThat(approved.status()).isEqualTo(AnomalyStatus.APPROVED);
        assertThatThrownBy(() -> review.reject(id).block()).isInstanceOf(ConflictException.class);
    }

    @Test
    void reviewingAnUnknownAnomalyFails() {
        assertThatThrownBy(() -> review.approve("nope").block()).isInstanceOf(NotFoundException.class);
    }

    private static class FakeInvoices implements InvoiceRepository {
        final List<Invoice> items = new ArrayList<>();

        public Mono<Invoice> save(Invoice invoice) {
            items.add(invoice);
            return Mono.just(invoice);
        }

        public Flux<Invoice> findByRunId(String runId) {
            return Flux.fromIterable(items.stream().filter(i -> runId.equals(i.runId())).toList());
        }
    }

    private static class FakeTransactions implements BankTransactionRepository {
        final List<BankTransaction> items = new ArrayList<>();

        public Mono<BankTransaction> save(BankTransaction transaction) {
            items.add(transaction);
            return Mono.just(transaction);
        }

        public Flux<BankTransaction> findByRunId(String runId) {
            return Flux.fromIterable(items.stream().filter(t -> runId.equals(t.runId())).toList());
        }
    }

    private static class FakeAnomalies implements AnomalyRepository {
        final List<Anomaly> items = new ArrayList<>();

        public Mono<Anomaly> save(Anomaly anomaly) {
            Anomaly stored = anomaly.id() != null ? anomaly : new Anomaly(UUID.randomUUID().toString(),
                    anomaly.runId(), anomaly.type(), anomaly.invoiceId(), anomaly.transactionId(),
                    anomaly.explanation(), anomaly.status());
            items.removeIf(a -> a.id().equals(stored.id()));
            items.add(stored);
            return Mono.just(stored);
        }

        public Mono<Anomaly> findById(String id) {
            return Mono.justOrEmpty(items.stream().filter(a -> a.id().equals(id)).findFirst());
        }

        public Flux<Anomaly> findByRunId(String runId) {
            return Flux.fromIterable(items.stream().filter(a -> runId.equals(a.runId())).toList());
        }

        public Mono<Void> deleteByRunId(String runId) {
            return Mono.fromRunnable(() -> items.removeIf(a -> runId.equals(a.runId())));
        }
    }
}
