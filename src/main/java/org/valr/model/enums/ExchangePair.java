package org.valr.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ExchangePair {
    BTCZAR(Currency.BTC, Currency.ZAR);

    private final Currency base;
    private final Currency quote;

    ExchangePair(Currency base, Currency quote) {
        this.base = base;
        this.quote = quote;
    }

    @JsonCreator
    public static ExchangePair fromString(String pair) {
        return Arrays.stream(ExchangePair.values())
                .filter(p -> p.name().equalsIgnoreCase(pair))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid crypto pair: " + pair));
    }
}