package com.reconcile.agent.adapter.in.web;

import com.reconcile.agent.application.port.in.StartRunUseCase;
import com.reconcile.agent.application.port.in.UploadedDocument;
import com.reconcile.agent.domain.model.RunSummary;
import java.util.List;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/runs")
public class RunController {

    private final StartRunUseCase useCase;

    public RunController(StartRunUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<RunSummary> start(@RequestPart("invoices") Flux<FilePart> invoices,
                                  @RequestPart("statement") Mono<FilePart> statement) {
        Mono<List<UploadedDocument>> invoiceDocuments = invoices.concatMap(this::toDocument).collectList();
        return invoiceDocuments.zipWith(statement.flatMap(this::toDocument))
                .flatMap(tuple -> useCase.start(tuple.getT1(), tuple.getT2()));
    }

    private Mono<UploadedDocument> toDocument(FilePart part) {
        return DataBufferUtils.join(part.content()).map(buffer -> {
            try {
                byte[] bytes = new byte[buffer.readableByteCount()];
                buffer.read(bytes);
                return new UploadedDocument(part.filename(), bytes);
            } finally {
                DataBufferUtils.release(buffer);
            }
        });
    }
}
