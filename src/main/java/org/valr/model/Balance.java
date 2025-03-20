package org.valr.model;

import lombok.*;
import org.valr.model.enums.Currency;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@AllArgsConstructor
@Setter
@Getter
@ToString
public class Balance {
    private final String userId;
    private final Map<Currency, BigDecimal> currentBalances;
    private final Map<Currency, BigDecimal> reserveBalances;

    public Balance(String userId) {
        this.userId = userId;
        currentBalances = new ConcurrentHashMap<>();
        reserveBalances = new ConcurrentHashMap<>();
        for (Currency currency : Currency.values()) {
            currentBalances.put(currency, BigDecimal.ZERO);
            reserveBalances.put(currency, BigDecimal.ZERO);
        }
    }

    public boolean hasSufficientBalance(Currency currency, BigDecimal amount) {
        return currentBalances.get(currency).compareTo(amount) >= 0;
    }

    public boolean hasSufficientReserveBalance(Currency currency, BigDecimal amount) {
        return reserveBalances.get(currency).compareTo(amount) >= 0;
    }
}