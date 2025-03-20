package org.valr.model;

import lombok.*;
import org.valr.model.enums.Currency;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Deposit {
    Currency currency;
    BigDecimal amount;
    Instant executed = Instant.now();
}
