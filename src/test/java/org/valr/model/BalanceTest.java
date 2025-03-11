package org.valr.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BalanceTest {

    @Test
    void testParameterizedConstructor() {
        Balance balance = new Balance("user123");

        assertEquals("user123", balance.getUserId());
        assertEquals(BigDecimal.ZERO, balance.getFiatBalance());
        assertEquals(BigDecimal.ZERO, balance.getCryptoBalance());
    }

    @Test
    void testHasSufficientFiatBalance() {
        Balance balance = new Balance("user123", BigDecimal.valueOf(100.00), BigDecimal.valueOf(0.05));

        assertTrue(balance.hasSufficientFiatBalance(BigDecimal.valueOf(50.00)));
        assertTrue(balance.hasSufficientFiatBalance(BigDecimal.valueOf(100.00)));
        assertFalse(balance.hasSufficientFiatBalance(BigDecimal.valueOf(101.00)));
    }

    @Test
    void testHasSufficientCryptoBalance() {
        Balance balance = new Balance("user123", BigDecimal.valueOf(100.00), BigDecimal.valueOf(0.05));

        assertTrue(balance.hasSufficientCryptoBalance(BigDecimal.valueOf(0.01)));
        assertTrue(balance.hasSufficientCryptoBalance(BigDecimal.valueOf(0.05)));
        assertFalse(balance.hasSufficientCryptoBalance(BigDecimal.valueOf(0.06)));
    }
}
