package com.reconcile.agent.adapter.in.web;

import com.reconcile.agent.application.port.in.ListInvoicesUseCase;
import com.reconcile.agent.domain.model.Invoice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final ListInvoicesUseCase useCase;

    public InvoiceController(ListInvoicesUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public Flux<Invoice> list(@RequestParam String runId) {
        return useCase.list(runId);
    }
}
