package org.valr.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.valr.model.enums.Currency;

import static org.junit.jupiter.api.Assertions.*;
import static org.valr.TestHelper.NUMBER;

//TOOO Add constraints validator check verifications
class BalanceTest {

    private Balance balance;

    @BeforeEach
    void setUp() {
        balance = new Balance("user123");
    }

    @Test
    void testInitialBalancesAreZero() {
        for (Currency currency : Currency.values()) {
            assertEquals(NUMBER(0), balance.getCurrentBalances().get(currency));
            assertEquals(NUMBER(0), balance.getReserveBalances().get(currency));
        }
    }

    @Test
    void testHasSufficientBalance() {
        Currency currency = Currency.BTC;
        balance.getCurrentBalances().put(currency, NUMBER(100));

        assertTrue(balance.hasSufficientBalance(currency, NUMBER(50)));
        assertFalse(balance.hasSufficientBalance(currency, NUMBER(150)));
    }

    @Test
    void testHasSufficientReserveBalance() {
        Currency currency = Currency.ZAR;
        balance.getReserveBalances().put(currency, NUMBER(200));

        assertTrue(balance.hasSufficientReserveBalance(currency, NUMBER(150)));
        assertFalse(balance.hasSufficientReserveBalance(currency, NUMBER(250)));
    }

    @Test
    void testModifyBalance() {
        Currency currency = Currency.BTC;
        balance.getCurrentBalances().put(currency, NUMBER(500));
        balance.getReserveBalances().put(currency, NUMBER(100));

        balance.getCurrentBalances().put(currency, NUMBER(600));
        balance.getReserveBalances().put(currency, NUMBER(200));

        assertEquals(NUMBER(600), balance.getCurrentBalances().get(currency));
        assertEquals(NUMBER(200), balance.getReserveBalances().get(currency));
    }

    @Test
    void testNegativeBalance() {
        Currency currency = Currency.BTC;
        balance.getCurrentBalances().put(currency, NUMBER(-10));

        assertFalse(balance.hasSufficientBalance(currency, NUMBER(1)));
    }

    @Test
    void testZeroBalanceEdgeCase() {
        Currency currency = Currency.ZAR;
        balance.getCurrentBalances().put(currency, NUMBER(0));

        assertTrue(balance.hasSufficientBalance(currency, NUMBER(0)));
        assertFalse(balance.hasSufficientBalance(currency, NUMBER(1)));
    }
}
