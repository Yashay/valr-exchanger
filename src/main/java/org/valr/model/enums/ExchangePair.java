package org.valr.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import static org.valr.util.enums.Common.fromBody;

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
        return fromBody(ExchangePair.class, pair);
    }
}