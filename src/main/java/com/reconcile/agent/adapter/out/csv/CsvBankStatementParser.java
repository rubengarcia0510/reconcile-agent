package com.reconcile.agent.adapter.out.csv;

import com.reconcile.agent.application.InvalidUploadException;
import com.reconcile.agent.application.port.out.BankStatementParser;
import com.reconcile.agent.domain.model.BankTransaction;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
class CsvBankStatementParser implements BankStatementParser {

    @Override
    public Flux<BankTransaction> parse(byte[] content, String runId) {
        return Flux.defer(() -> {
            String text = new String(content, StandardCharsets.UTF_8).replace("\uFEFF", "");
            List<String> lines = text.lines().toList();
            List<BankTransaction> rows = new ArrayList<>();
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isBlank() || (i == 0 && line.toLowerCase().startsWith("date"))) {
                    continue;
                }
                rows.add(toTransaction(line, i + 1, runId));
            }
            return Flux.fromIterable(rows);
        });
    }

    private BankTransaction toTransaction(String line, int lineNumber, String runId) {
        List<String> fields = split(line);
        if (fields.size() < 4) {
            throw new InvalidUploadException(
                    "Invalid CSV line %d: expected date,description,amount,currency,reference".formatted(lineNumber));
        }
        try {
            return new BankTransaction(null, runId, LocalDate.parse(fields.get(0)), fields.get(1),
                    new BigDecimal(fields.get(2)), fields.get(3), fields.size() > 4 ? fields.get(4) : "");
        } catch (DateTimeParseException | NumberFormatException e) {
            throw new InvalidUploadException("Invalid CSV line %d: %s".formatted(lineNumber, e.getMessage()));
        }
    }

    private static List<String> split(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (char c : line.toCharArray()) {
            if (c == '"') {
                quoted = !quoted;
            } else if (c == ',' && !quoted) {
                fields.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString().trim());
        return fields;
    }
}
