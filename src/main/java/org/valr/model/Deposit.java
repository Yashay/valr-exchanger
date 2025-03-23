package org.valr.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.valr.model.enums.Currency;
import org.valr.util.enums.ValidEnum;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Getter
@Setter
@AllArgsConstructor
public class Deposit {

    @NotNull(message = "Side is required")
    @ValidEnum(enumClass = Currency.class, message = "Must match a currency")
    final Currency currency;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.000000001", message = "Amount must be greater than zero")
    final BigDecimal amount;

    final Instant intiated;

    public Deposit() {
        this.currency = null;
        this.amount = BigDecimal.ZERO;
        this.intiated = Instant.now();
    }

}
