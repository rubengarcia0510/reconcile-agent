package com.reconcile.agent.adapter.out.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.reconcile.agent.application.port.out.InvoiceExtractor;
import com.reconcile.agent.domain.model.Invoice;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

@Component
class VultrInvoiceExtractor implements InvoiceExtractor {

    private static final String SYSTEM_PROMPT = """
            Extract invoice data from the text the user sends.
            Reply with ONE JSON object only. No markdown, no explanations.
            Fields:
            - supplier: string
            - number: string, the invoice number. If the document shows a point of sale and a voucher number separately, combine them as POINT-NUMBER (for example 0005-00001234). Ignore purchase order, delivery note, CAE and bank account numbers.
            - amount: number, the invoice total, with a decimal point and no thousands separators
            - currency: ISO 4217 code
            - issueDate: string, YYYY-MM-DD (dates in the text are usually day/month/year)
            Use null for any field that is missing.
            """;

    private final WebClient webClient;
    private final VultrInferenceProperties props;
    private final JsonMapper jsonMapper;

    VultrInvoiceExtractor(WebClient vultrInferenceWebClient, VultrInferenceProperties props, JsonMapper jsonMapper) {
        this.webClient = vultrInferenceWebClient;
        this.props = props;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public Mono<Invoice> extract(String rawText) {
        Map<String, Object> body = Map.of(
                "model", props.model(),
                "max_tokens", props.maxTokens(),
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", rawText)));

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(ChatResponse.class)
                .timeout(Duration.ofSeconds(60))
                .map(this::toInvoice);
    }

    private Invoice toInvoice(ChatResponse response) {
        String content = (response.choices() == null || response.choices().isEmpty()
                || response.choices().get(0).message() == null)
                ? null : response.choices().get(0).message().content();
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("The model returned an empty answer (try a higher max-tokens)");
        }
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start < 0 || end < start) {
            throw new IllegalStateException("The model answer does not contain a JSON object");
        }
        ExtractedInvoice e = jsonMapper.readValue(content.substring(start, end + 1), ExtractedInvoice.class);
        return new Invoice(null, null, e.supplier(), e.number(), e.amount(), e.currency(), e.issueDate());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ChatResponse(List<Choice> choices) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Choice(Message message) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Message(String content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ExtractedInvoice(String supplier, String number, BigDecimal amount,
                            String currency, LocalDate issueDate) {}
}
