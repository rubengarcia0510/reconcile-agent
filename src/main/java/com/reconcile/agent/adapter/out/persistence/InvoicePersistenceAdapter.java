package com.reconcile.agent.adapter.out.persistence;

import com.reconcile.agent.application.port.out.InvoiceRepository;
import com.reconcile.agent.domain.model.Invoice;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
class InvoicePersistenceAdapter implements InvoiceRepository {

    @Document("invoices")
    record Doc(@Id String id, String runId, String supplier, String number,
               BigDecimal amount, String currency, LocalDate issueDate) {}

    private final InvoiceMongoRepository repository;

    InvoicePersistenceAdapter(InvoiceMongoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Invoice> save(Invoice i) {
        String id = i.id() != null ? i.id() : UUID.randomUUID().toString();
        return repository.save(new Doc(id, i.runId(), i.supplier(), i.number(),
                i.amount(), i.currency(), i.issueDate())).map(this::toDomain);
    }

    @Override
    public Flux<Invoice> findByRunId(String runId) {
        return repository.findByRunId(runId).map(this::toDomain);
    }

    private Invoice toDomain(Doc d) {
        return new Invoice(d.id(), d.runId(), d.supplier(), d.number(),
                d.amount(), d.currency(), d.issueDate());
    }
}
