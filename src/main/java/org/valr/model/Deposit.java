package org.valr.model;

import lombok.*;
import org.valr.model.enums.Currency;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Getter
@Setter
@AllArgsConstructor
public class Deposit {
    final Currency currency;
    final BigDecimal amount;
    final Instant executed;

    public Deposit() {
        this.currency = null;
        this.amount = BigDecimal.ZERO;
        this.executed = Instant.now();
    }

}
