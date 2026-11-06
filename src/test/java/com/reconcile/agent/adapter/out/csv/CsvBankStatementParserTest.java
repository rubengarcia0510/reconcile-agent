package com.reconcile.agent.adapter.out.csv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.reconcile.agent.domain.model.BankTransaction;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class CsvBankStatementParserTest {

    private final CsvBankStatementParser parser = new CsvBankStatementParser();

    @Test
    void parsesRowsAndSkipsTheHeader() {
        String csv = "date,description,amount,currency,reference\n"
                + "2026-10-16,Transfer to ACME,-184250.00,ARS,FC 0001-00045821\n"
                + "2026-10-20,Customer deposit,310000.00,ARS,\n";

        List<BankTransaction> rows = parser.parse(csv.getBytes(StandardCharsets.UTF_8), "run")
                .collectList().block();

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).amount()).isEqualByComparingTo("-184250.00");
        assertThat(rows.get(0).reference()).isEqualTo("FC 0001-00045821");
        assertThat(rows.get(1).reference()).isEmpty();
    }

    @Test
    void rejectsAnInvalidLineWithItsNumber() {
        String csv = "date,description,amount,currency,reference\nnot-a-date,x,1,ARS,\n";

        assertThatThrownBy(() -> parser.parse(csv.getBytes(StandardCharsets.UTF_8), "run")
                .collectList().block())
                .hasMessageContaining("line 2");
    }
}
