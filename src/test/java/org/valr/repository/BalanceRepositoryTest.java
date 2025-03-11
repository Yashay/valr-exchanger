package org.valr.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.valr.model.Balance;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BalanceRepositoryTest {
    private BalanceRepository balanceRepository;

    @BeforeEach
    void setUp() {
        balanceRepository = new BalanceRepository();
    }

    @Test
    void testGetBalanceCreatesNewBalanceIfNotExists() {
        String userId = "user123";
        Balance balance = balanceRepository.getBalance(userId);

        assertNotNull(balance);
        assertEquals(userId, balance.getUserId());
        assertEquals(BigDecimal.ZERO, balance.getFiatBalance());
        assertEquals(BigDecimal.ZERO, balance.getCryptoBalance());
    }

    @Test
    void testGetBalanceReturnsExistingBalance() {
        String userId = "user123";
        // Use BigDecimal.valueOf() for clean initialization
        Balance initialBalance = new Balance(userId, BigDecimal.valueOf(100.00), BigDecimal.valueOf(0.50));
        balanceRepository.getBalance(userId).setFiatBalance(BigDecimal.valueOf(200.00));
        balanceRepository.getBalance(userId).setCryptoBalance(BigDecimal.valueOf(1));

        Balance retrievedBalance = balanceRepository.getBalance(userId);

        assertNotNull(retrievedBalance);
        assertEquals(BigDecimal.valueOf(200.00), retrievedBalance.getFiatBalance());
        assertEquals(BigDecimal.valueOf(1), retrievedBalance.getCryptoBalance());
    }
}
