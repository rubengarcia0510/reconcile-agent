package com.reconcile.agent.application.service;

import com.reconcile.agent.application.port.in.ExtractInvoiceUseCase;
import com.reconcile.agent.application.port.out.InvoiceExtractor;
import com.reconcile.agent.application.port.out.InvoiceRepository;
import com.reconcile.agent.domain.model.Invoice;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ExtractInvoiceService implements ExtractInvoiceUseCase {

    private final InvoiceExtractor extractor;
    private final InvoiceRepository invoices;

    public ExtractInvoiceService(InvoiceExtractor extractor, InvoiceRepository invoices) {
        this.extractor = extractor;
        this.invoices = invoices;
    }

    @Override
    public Mono<Invoice> extract(String rawText, String runId) {
        return extractor.extract(rawText)
                .map(invoice -> invoice.withRunId(runId))
                .flatMap(invoices::save);
    }
}
