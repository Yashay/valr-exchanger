package org.valr.model.enums;

public enum TimeInForce {
    FOK, // [IMMEDIATE] Fill or Kill -> Must fill the entire order immediately OTHERWISE cancel it
    IOC, // [IMMEDIATE] Immediate or Cancel -> Fill immediately with available liquidity then cancels the remainder
    GTC; // [PARTIAL FILL] Good Till Cancel -> Fill until the order is completely filled
}
