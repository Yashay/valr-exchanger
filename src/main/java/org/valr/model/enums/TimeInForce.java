package org.valr.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import static org.valr.util.enums.Common.fromBody;

public enum TimeInForce {
    FOK, // [IMMEDIATE] Fill or Kill -> Must fill the entire order immediately OTHERWISE cancel it
    IOC, // [IMMEDIATE] Immediate or Cancel -> Fill immediately with available liquidity then cancels the remainder
    GTC; // [PARTIAL FILL] Good Till Cancel -> Fill until the order is completely filled

    @JsonCreator
    public static TimeInForce fromString(String timeInForce) {
        return fromBody(TimeInForce.class, timeInForce);
    }
}
