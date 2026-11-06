package com.reconcile.agent.adapter.in.web;

import com.reconcile.agent.application.port.in.ExtractInvoiceUseCase;
import com.reconcile.agent.application.port.in.ListInvoicesUseCase;
import com.reconcile.agent.domain.model.Invoice;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/invoices")
public class ExtractionController {

    private final ExtractInvoiceUseCase extractUseCase;
    private final ListInvoicesUseCase listUseCase;

    public ExtractionController(ExtractInvoiceUseCase extractUseCase, ListInvoicesUseCase listUseCase) {
        this.extractUseCase = extractUseCase;
        this.listUseCase = listUseCase;
    }

    @PostMapping(value = "/extract", consumes = MediaType.TEXT_PLAIN_VALUE)
    public Mono<Invoice> extract(@RequestBody String text,
                                 @RequestParam(defaultValue = "demo") String runId) {
        return extractUseCase.extract(text, runId);
    }

    @GetMapping
    public Flux<Invoice> list(@RequestParam(defaultValue = "demo") String runId) {
        return listUseCase.list(runId);
    }
}
