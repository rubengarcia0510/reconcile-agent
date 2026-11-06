package com.reconcile.agent.application.service;

import com.reconcile.agent.application.NotFoundException;
import com.reconcile.agent.application.port.in.AnalyzeRunUseCase;
import com.reconcile.agent.application.port.out.AnomalyRepository;
import com.reconcile.agent.application.port.out.BankTransactionRepository;
import com.reconcile.agent.application.port.out.InvoiceRepository;
import com.reconcile.agent.domain.model.AnalysisResult;
import com.reconcile.agent.domain.model.Anomaly;
import com.reconcile.agent.domain.model.BankTransaction;
import com.reconcile.agent.domain.model.Invoice;
import com.reconcile.agent.domain.model.ReconciliationResult;
import com.reconcile.agent.domain.service.AnomalyDetector;
import com.reconcile.agent.domain.service.ReconciliationEngine;
import java.util.List;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AnalyzeRunService implements AnalyzeRunUseCase {

    private final InvoiceRepository invoices;
    private final BankTransactionRepository transactions;
    private final AnomalyRepository anomalies;
    private final ReconciliationEngine engine = ReconciliationEngine.withDefaults();
    private final AnomalyDetector detector = AnomalyDetector.withDefaults();

    public AnalyzeRunService(InvoiceRepository invoices, BankTransactionRepository transactions,
                             AnomalyRepository anomalies) {
        this.invoices = invoices;
        this.transactions = transactions;
        this.anomalies = anomalies;
    }

    @Override
    public Mono<AnalysisResult> analyze(String runId) {
        return Mono.zip(invoices.findByRunId(runId).collectList(),
                        transactions.findByRunId(runId).collectList())
                .flatMap(tuple -> analyze(runId, tuple.getT1(), tuple.getT2()));
    }

    private Mono<AnalysisResult> analyze(String runId, List<Invoice> runInvoices,
                                         List<BankTransaction> runTransactions) {
        if (runInvoices.isEmpty() && runTransactions.isEmpty()) {
            return Mono.error(new NotFoundException("Run not found: " + runId));
        }
        ReconciliationResult reconciliation = engine.reconcile(runInvoices, runTransactions);
        List<Anomaly> detected = detector.detect(runInvoices, reconciliation);
        return anomalies.deleteByRunId(runId)
                .thenMany(Flux.fromIterable(detected).concatMap(anomalies::save))
                .collectList()
                .map(saved -> new AnalysisResult(runId, reconciliation.matches().size(),
                        reconciliation.unmatchedInvoices().size(),
                        reconciliation.unmatchedTransactions().size(), saved));
    }
}
