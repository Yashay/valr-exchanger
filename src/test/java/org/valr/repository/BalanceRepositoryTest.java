package org.valr.repository;

import org.valr.model.Balance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BalanceRepositoryTest {

    private BalanceRepository balanceRepository;

    @BeforeEach
    void setUp() {
        balanceRepository = new BalanceRepository();
    }

    @Test
    void testGetBalanceNewUser() {
        String userId = "user123";
        Balance balance = balanceRepository.getBalance(userId);
        assertNotNull(balance);
        assertEquals(userId, balance.getUserId());
    }

    @Test
    void testGetBalanceExistingUser() {
        String userId = "user123";
        Balance initialBalance = balanceRepository.getBalance(userId);
        Balance retrievedBalance = balanceRepository.getBalance(userId);
        assertSame(initialBalance, retrievedBalance);
    }

    @Test
    void testGetBalanceMultipleUsers() {
        String userId1 = "user123";
        String userId2 = "user456";
        Balance balance1 = balanceRepository.getBalance(userId1);
        Balance balance2 = balanceRepository.getBalance(userId2);
        assertNotSame(balance1, balance2);
        assertEquals(userId1, balance1.getUserId());
        assertEquals(userId2, balance2.getUserId());
    }

    @Test
    void testGetBalanceUserIdNotNull() {
        String userId = "user123";
        Balance balance = balanceRepository.getBalance(userId);
        assertNotNull(balance.getUserId());
        assertEquals(userId, balance.getUserId());
    }

}
