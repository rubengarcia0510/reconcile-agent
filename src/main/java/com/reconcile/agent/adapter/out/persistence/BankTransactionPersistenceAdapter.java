package com.reconcile.agent.adapter.out.persistence;

import com.reconcile.agent.application.port.out.BankTransactionRepository;
import com.reconcile.agent.domain.model.BankTransaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
class BankTransactionPersistenceAdapter implements BankTransactionRepository {

    @Document("bank_transactions")
    record Doc(@Id String id, String runId, LocalDate date, String description,
               BigDecimal amount, String currency, String reference) {}

    private final BankTransactionMongoRepository repository;

    BankTransactionPersistenceAdapter(BankTransactionMongoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<BankTransaction> save(BankTransaction t) {
        String id = t.id() != null ? t.id() : UUID.randomUUID().toString();
        return repository.save(new Doc(id, t.runId(), t.date(), t.description(),
                t.amount(), t.currency(), t.reference())).map(this::toDomain);
    }

    @Override
    public Flux<BankTransaction> findByRunId(String runId) {
        return repository.findByRunId(runId).map(this::toDomain);
    }

    private BankTransaction toDomain(Doc d) {
        return new BankTransaction(d.id(), d.runId(), d.date(), d.description(),
                d.amount(), d.currency(), d.reference());
    }
}
