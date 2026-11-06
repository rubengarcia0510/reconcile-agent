package com.reconcile.agent.adapter.out.pdf;

import com.reconcile.agent.application.port.out.DocumentTextReader;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
class PdfBoxTextReader implements DocumentTextReader {

    @Override
    public Mono<String> read(byte[] content) {
        return Mono.fromCallable(() -> {
            try (PDDocument document = Loader.loadPDF(content)) {
                return new PDFTextStripper().getText(document);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
