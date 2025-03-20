package org.valr.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.valr.model.Balance;
import org.valr.model.Order;
import org.valr.model.enums.Currency;
import org.valr.model.enums.Side;
import org.valr.repository.BalanceRepository;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.valr.TestHelper.NUMBER;
import static org.valr.TestHelper.createOrder;

class BalanceServiceTest {

    private BalanceService balanceService;
    private BalanceRepository balanceRepository;
    private Balance userBalance;

    @BeforeEach
    void setUp() {
        balanceRepository = mock(BalanceRepository.class);
        balanceService = new BalanceService(balanceRepository);
        userBalance = mockUserBalance("userId", NUMBER(0), NUMBER(0));
    }

    @Test
    void testAddBalance() {
        balanceService.add("userId", Currency.BTC, NUMBER(1));

        assertEquals(NUMBER(1), userBalance.getCurrentBalances().get(Currency.BTC));

        balanceService.add("userId", Currency.BTC, NUMBER(3));
        assertEquals(NUMBER(4), userBalance.getCurrentBalances().get(Currency.BTC));
    }

    @Test
    void testSubtractBalance() {
        balanceService.add("userId", Currency.ZAR, NUMBER(1000));

        balanceService.subtract("userId", Currency.ZAR, NUMBER(200));
        assertEquals(NUMBER(800), userBalance.getCurrentBalances().get(Currency.ZAR));

        balanceService.subtract("userId", Currency.ZAR, NUMBER(900));
        assertEquals(NUMBER(-100), userBalance.getCurrentBalances().get(Currency.ZAR));
    }

    @Test
    void testReserveBalance_Success() {
        balanceService.add("userId", Currency.ZAR, NUMBER(1000));

        boolean result = balanceService.reserve("userId", Currency.ZAR, NUMBER(500));

        assertTrue(result);
        assertEquals(NUMBER(500), userBalance.getCurrentBalances().get(Currency.ZAR));
        assertEquals(NUMBER(500), userBalance.getReserveBalances().get(Currency.ZAR));
    }

    @Test
    void testReserveBalance_FailDueToInsufficientFunds() {
        balanceService.add("userId", Currency.ZAR, NUMBER(100));

        boolean result = balanceService.reserve("userId", Currency.ZAR, NUMBER(200));

        assertFalse(result);
        assertEquals(NUMBER(100), userBalance.getCurrentBalances().get(Currency.ZAR));
        assertEquals(NUMBER(0), userBalance.getReserveBalances().get(Currency.ZAR));
    }

    @Test
    void testUnreserveBalance_Success() {
        balanceService.add("userId", Currency.ZAR, NUMBER(1000));
        balanceService.reserve("userId", Currency.ZAR, NUMBER(500));

        boolean result = balanceService.unreserve("userId", Currency.ZAR, NUMBER(200));

        assertTrue(result);
        assertEquals(NUMBER(700), userBalance.getCurrentBalances().get(Currency.ZAR));
        assertEquals(NUMBER(300), userBalance.getReserveBalances().get(Currency.ZAR));
    }

    @Test
    void testUnreserveBalance_FailDueToInsufficientReservedFunds() {
        balanceService.add("userId", Currency.ZAR, NUMBER(1000));
        balanceService.reserve("userId", Currency.ZAR, NUMBER(300));

        boolean result = balanceService.unreserve("userId", Currency.ZAR, NUMBER(500));

        assertFalse(result);
        assertEquals(NUMBER(700), userBalance.getCurrentBalances().get(Currency.ZAR));
        assertEquals(NUMBER(300), userBalance.getReserveBalances().get(Currency.ZAR));
    }

    @Test
    void testReserveOnOrder_BuyOrder() {
        Order order = createOrder("userId", Side.BUY, NUMBER(100), NUMBER(10));

        balanceService.add("userId", Currency.ZAR, NUMBER(1000));

        boolean result = balanceService.reserveOnOrder(order);

        assertTrue(result);
        assertEquals(NUMBER(1000), userBalance.getReserveBalances().get(Currency.ZAR));
    }

    @Test
    void testUnreserveOnOrder_BuyOrder() {
        Order order = createOrder("userId", Side.BUY, NUMBER(500), NUMBER(2));

        balanceService.add("userId", Currency.ZAR, NUMBER(1000));
        balanceService.reserveOnOrder(order);

        boolean result = balanceService.unreserveOnOrder(order);

        assertTrue(result);
        assertEquals(NUMBER(1000), userBalance.getCurrentBalances().get(Currency.ZAR));
        assertEquals(NUMBER(0), userBalance.getReserveBalances().get(Currency.ZAR));
    }

    @Test
    void testAdjustBalancesForTrade_BuySide() {
        Order takerOrder = createOrder("taker", Side.BUY, NUMBER(1000), NUMBER(2));
        Order makerOrder = createOrder("maker", Side.SELL, NUMBER(1000), NUMBER(2));

        Balance takerBalance = mockUserBalance("taker", NUMBER(0), NUMBER(2000));
        Balance makerBalance = mockUserBalance("maker", NUMBER(2), NUMBER(0));

        balanceService.adjustBalancesForTrade(takerOrder, makerOrder, NUMBER(2));

        assertEquals(NUMBER(2000), makerBalance.getCurrentBalances().get(Currency.ZAR));
        assertEquals(NUMBER(2), takerBalance.getCurrentBalances().get(Currency.BTC));
    }

    @Test
    void testAdjustBalancesForTrade_SellSide() {
        Order takerOrder = createOrder("taker", Side.SELL, NUMBER(1000), NUMBER(2));
        Order makerOrder = createOrder("maker", Side.BUY, NUMBER(1000), NUMBER(2));

        Balance takerBalance = mockUserBalance("taker", NUMBER(2), NUMBER(0));
        Balance makerBalance = mockUserBalance("maker", NUMBER(0), NUMBER(2000));

        balanceService.adjustBalancesForTrade(takerOrder, makerOrder, NUMBER(2));

        assertEquals(NUMBER(2000), takerBalance.getCurrentBalances().get(Currency.ZAR));
        assertEquals(NUMBER(2), makerBalance.getCurrentBalances().get(Currency.BTC));
    }


    private Balance mockUserBalance(String userId, BigDecimal btc, BigDecimal zar) {
        Balance userBalance = new Balance(userId);
        Map<Currency, BigDecimal> currentBalances = new EnumMap<>(Currency.class);
        currentBalances.put(Currency.BTC, btc);
        currentBalances.put(Currency.ZAR, zar);

        userBalance.getCurrentBalances().putAll(currentBalances);
        when(balanceRepository.getBalance(userId)).thenReturn(userBalance);

        return userBalance;
    }
}
