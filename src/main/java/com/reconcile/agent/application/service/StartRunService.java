package com.reconcile.agent.application.service;

import com.reconcile.agent.application.InvalidUploadException;
import com.reconcile.agent.application.port.in.ExtractInvoiceUseCase;
import com.reconcile.agent.application.port.in.StartRunUseCase;
import com.reconcile.agent.application.port.in.UploadedDocument;
import com.reconcile.agent.application.port.out.BankStatementParser;
import com.reconcile.agent.application.port.out.BankTransactionRepository;
import com.reconcile.agent.application.port.out.DocumentTextReader;
import com.reconcile.agent.domain.model.RunSummary;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class StartRunService implements StartRunUseCase {

    private static final int CONCURRENCY = 4;
    private static final int MAX_INVOICES = 30;

    private final DocumentTextReader textReader;
    private final ExtractInvoiceUseCase extractInvoice;
    private final BankStatementParser statementParser;
    private final BankTransactionRepository transactions;

    public StartRunService(DocumentTextReader textReader, ExtractInvoiceUseCase extractInvoice,
                           BankStatementParser statementParser, BankTransactionRepository transactions) {
        this.textReader = textReader;
        this.extractInvoice = extractInvoice;
        this.statementParser = statementParser;
        this.transactions = transactions;
    }

    @Override
    public Mono<RunSummary> start(List<UploadedDocument> invoiceFiles, UploadedDocument statement) {
        if (invoiceFiles.isEmpty()) {
            return Mono.error(new InvalidUploadException("At least one invoice PDF is required"));
        }
        if (invoiceFiles.size() > MAX_INVOICES) {
            return Mono.error(new InvalidUploadException("At most " + MAX_INVOICES + " invoices per run"));
        }
        String runId = UUID.randomUUID().toString();
        Mono<Long> loaded = statementParser.parse(statement.content(), runId)
                .flatMap(transactions::save)
                .count();
        return loaded.flatMap(count -> Flux.fromIterable(invoiceFiles)
                .flatMap(file -> processInvoice(file, runId), CONCURRENCY)
                .collectList()
                .map(outcomes -> summarize(runId, count, outcomes)));
    }

    private Mono<InvoiceOutcome> processInvoice(UploadedDocument file, String runId) {
        return textReader.read(file.content())
                .flatMap(text -> text.isBlank()
                        ? Mono.error(new InvalidUploadException("No text found in " + file.filename()))
                        : extractInvoice.extract(text, runId))
                .map(invoice -> InvoiceOutcome.ok(file.filename()))
                .onErrorResume(error -> Mono.just(
                        InvoiceOutcome.failed(file.filename(), String.valueOf(error.getMessage()))));
    }

    private RunSummary summarize(String runId, long transactionsLoaded, List<InvoiceOutcome> outcomes) {
        List<String> failed = outcomes.stream().filter(InvoiceOutcome::failed)
                .map(InvoiceOutcome::filename).sorted().toList();
        return new RunSummary(runId, outcomes.size() - failed.size(), failed.size(), transactionsLoaded, failed);
    }

    private record InvoiceOutcome(String filename, String error) {
        static InvoiceOutcome ok(String filename) {
            return new InvoiceOutcome(filename, null);
        }

        static InvoiceOutcome failed(String filename, String error) {
            return new InvoiceOutcome(filename, error);
        }

        boolean failed() {
            return error != null;
        }
    }
}
