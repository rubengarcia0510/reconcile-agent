package com.reconcile.agent.application.service;

import com.reconcile.agent.application.port.in.ExtractInvoiceUseCase;
import com.reconcile.agent.application.port.out.InvoiceExtractor;
import com.reconcile.agent.domain.model.Invoice;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ExtractInvoiceService implements ExtractInvoiceUseCase {

    private final InvoiceExtractor extractor;

    public ExtractInvoiceService(InvoiceExtractor extractor) {
        this.extractor = extractor;
    }

    @Override
    public Mono<Invoice> extract(String rawText) {
        return extractor.extract(rawText);
    }
}
