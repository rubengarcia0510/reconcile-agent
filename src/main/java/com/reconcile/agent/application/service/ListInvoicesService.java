package com.reconcile.agent.application.service;

import com.reconcile.agent.application.port.in.ListInvoicesUseCase;
import com.reconcile.agent.application.port.out.InvoiceRepository;
import com.reconcile.agent.domain.model.Invoice;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ListInvoicesService implements ListInvoicesUseCase {

    private final InvoiceRepository invoices;

    public ListInvoicesService(InvoiceRepository invoices) {
        this.invoices = invoices;
    }

    @Override
    public Flux<Invoice> list(String runId) {
        return invoices.findByRunId(runId);
    }
}
