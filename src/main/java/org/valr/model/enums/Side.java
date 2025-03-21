package org.valr.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import static org.valr.util.enums.Common.fromBody;

public enum Side {
    SELL,
    BUY;

    @JsonCreator
    public static Side fromString(String side) {
        return fromBody(Side.class, side);
    }
}
