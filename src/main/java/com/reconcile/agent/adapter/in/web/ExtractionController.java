package com.reconcile.agent.adapter.in.web;

import com.reconcile.agent.application.port.in.ExtractInvoiceUseCase;
import com.reconcile.agent.domain.model.Invoice;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/invoices")
public class ExtractionController {

    private final ExtractInvoiceUseCase useCase;

    public ExtractionController(ExtractInvoiceUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping(value = "/extract", consumes = MediaType.TEXT_PLAIN_VALUE)
    public Mono<Invoice> extract(@RequestBody String text) {
        return useCase.extract(text);
    }
}
