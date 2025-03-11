package org.valr.model;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class Balance {
    private String userId;
    private BigDecimal fiatBalance;
    private BigDecimal cryptoBalance;

    public Balance(String userId) {
        this.userId = userId;
        this.fiatBalance = BigDecimal.ZERO;
        this.cryptoBalance = BigDecimal.ZERO;
    }

    public boolean hasSufficientFiatBalance(BigDecimal amount) {
        return fiatBalance.compareTo(amount) >= 0;
    }

    public boolean hasSufficientCryptoBalance(BigDecimal amount) {
        return cryptoBalance.compareTo(amount) >= 0;
    }
}