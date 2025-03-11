package org.valr.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.valr.model.Balance;
import org.valr.repository.BalanceRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BalanceServiceTest {

    private BalanceService balanceService;
    private BalanceRepository balanceRepository;

    @BeforeEach
    void setUp() {
        balanceRepository = mock(BalanceRepository.class);
        balanceService = new BalanceService(balanceRepository);
    }

    @Test
    void testGetUserBalanceReturnsBalance() {
        String userId = "user123";
        Balance expectedBalance = new Balance(userId, BigDecimal.valueOf(100), BigDecimal.valueOf(1));

        when(balanceRepository.getBalance(userId)).thenReturn(expectedBalance);

        Balance result = balanceService.getUserBalance(userId);

        assertNotNull(result);
        assertEquals(expectedBalance, result);
        verify(balanceRepository).getBalance(userId);
    }

    @Test
    void testAddFiatIncreasesFiatBalance() {
        String userId = "user123";
        Balance balance = new Balance(userId, BigDecimal.valueOf(100), BigDecimal.ZERO);

        when(balanceRepository.getBalance(userId)).thenReturn(balance);

        balanceService.addFiat(userId, BigDecimal.valueOf(50));

        assertEquals(BigDecimal.valueOf(150), balance.getFiatBalance());
        verify(balanceRepository, times(1)).getBalance(userId);
    }

    @Test
    void testAddCryptoIncreasesCryptoBalance() {
        String userId = "user123";
        Balance balance = new Balance(userId, BigDecimal.ZERO, BigDecimal.valueOf(0.5));

        when(balanceRepository.getBalance(userId)).thenReturn(balance);

        balanceService.addCrypto(userId, BigDecimal.valueOf(0.25));

        assertEquals(BigDecimal.valueOf(0.75), balance.getCryptoBalance());
        verify(balanceRepository, times(1)).getBalance(userId);
    }

    @Test
    void testDeductFiatSucceedsWhenEnoughBalance() {
        String userId = "user123";
        Balance balance = new Balance(userId, BigDecimal.valueOf(200), BigDecimal.ZERO);

        when(balanceRepository.getBalance(userId)).thenReturn(balance);

        boolean result = balanceService.deductFiat(userId, BigDecimal.valueOf(50));

        assertTrue(result);
        assertEquals(BigDecimal.valueOf(150), balance.getFiatBalance());
        verify(balanceRepository, times(1)).getBalance(userId);
    }

    @Test
    void testDeductFiatFailsWhenInsufficientBalance() {
        String userId = "user123";
        Balance balance = new Balance(userId, BigDecimal.valueOf(30), BigDecimal.ZERO);

        when(balanceRepository.getBalance(userId)).thenReturn(balance);

        boolean result = balanceService.deductFiat(userId, BigDecimal.valueOf(50));

        assertFalse(result);
        assertEquals(BigDecimal.valueOf(30), balance.getFiatBalance());
        verify(balanceRepository, times(1)).getBalance(userId);
    }

    @Test
    void testDeductCryptoSucceedsWhenEnoughBalance() {
        String userId = "user123";
        Balance balance = new Balance(userId, BigDecimal.ZERO, BigDecimal.valueOf(2.5));

        when(balanceRepository.getBalance(userId)).thenReturn(balance);

        boolean result = balanceService.deductCrypto(userId, BigDecimal.valueOf(1.0));

        assertTrue(result);
        assertEquals(BigDecimal.valueOf(1.5), balance.getCryptoBalance());
        verify(balanceRepository, times(1)).getBalance(userId);
    }

    @Test
    void testDeductCryptoFailsWhenInsufficientBalance() {
        String userId = "user123";
        Balance balance = new Balance(userId, BigDecimal.ZERO, BigDecimal.valueOf(0.3));

        when(balanceRepository.getBalance(userId)).thenReturn(balance);

        boolean result = balanceService.deductCrypto(userId, BigDecimal.valueOf(1.0));

        assertFalse(result);
        assertEquals(BigDecimal.valueOf(0.3), balance.getCryptoBalance());
        verify(balanceRepository, times(1)).getBalance(userId);
    }
}
