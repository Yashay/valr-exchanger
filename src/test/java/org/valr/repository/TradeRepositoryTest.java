package org.valr.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.valr.model.Order;
import org.valr.model.Trade;
import org.valr.model.enums.Side;

import java.util.concurrent.ConcurrentSkipListSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.valr.TestHelper.NUMBER;

class TradeRepositoryTest {

    private TradeRepository tradeRepository;
    private Order sellerOrder;
    private Order buyerOrder;

    @BeforeEach
    void setUp() {
        tradeRepository = new TradeRepository();
        buyerOrder = Order.builder()
                .userId("user1")
                .side(Side.BUY)
                .quantity(NUMBER(10))
                .price(NUMBER(50000))
                .build();
        sellerOrder = Order.builder()
                .userId("user2")
                .side(Side.SELL)
                .quantity(NUMBER(10))
                .price(NUMBER(50000))
                .build();
    }

    @Test
    void testAddTradeValidTradeShouldBeStored() {
        Trade trade = new Trade(buyerOrder, sellerOrder, NUMBER(50000), NUMBER(10));

        tradeRepository.addTrade(trade);

        ConcurrentSkipListSet<Trade> user1Trades = tradeRepository.getTradeHistory("user1");
        ConcurrentSkipListSet<Trade> user2Trades = tradeRepository.getTradeHistory("user2");

        assertEquals(1, user1Trades.size(), "User1 should have 1 trade.");
        assertEquals(1, user2Trades.size(), "User2 should have 1 trade.");
        assertTrue(user1Trades.contains(trade), "User1's trade history should contain the trade.");
        assertTrue(user2Trades.contains(trade), "User2's trade history should contain the trade.");
    }

    @Test
    void testGetTradeHistoryNoTradesShouldReturnEmptySet() {
        ConcurrentSkipListSet<Trade> user1Trades = tradeRepository.getTradeHistory("user1");
        assertNotNull(user1Trades, "Trade history should not be null.");
        assertTrue(user1Trades.isEmpty(), "Trade history should be empty for a user with no trades.");
    }

    @Test
    void testGetTradeHistoryExistingTradesShouldReturnCorrectTrades() {
        Trade trade = new Trade(buyerOrder, sellerOrder, NUMBER(50000), NUMBER(10));

        tradeRepository.addTrade(trade);

        ConcurrentSkipListSet<Trade> user1Trades = tradeRepository.getTradeHistory("user1");
        assertEquals(1, user1Trades.size(), "User1 should have 1 trade.");
        assertTrue(user1Trades.contains(trade), "User1 should contain the trade.");
    }

    @Test
    void testAddTradeMultipleTradesShouldBeStoredForAllUsers() {
        Trade tradeUser1User2 = new Trade(buyerOrder, sellerOrder, NUMBER(50000), NUMBER(10));
        Order user1BuyOrderSecond = Order.builder()
                .userId("user1")
                .side(Side.BUY)
                .quantity(NUMBER(10))
                .price(NUMBER(55000))
                .build();
        Order user3SellOrder = Order.builder()
                .userId("user3")
                .side(Side.SELL)
                .quantity(NUMBER(10))
                .price(NUMBER(55000))
                .build();
        Trade tradeUser1User3 = new Trade(user1BuyOrderSecond, user3SellOrder, NUMBER(55000), NUMBER(10));

        tradeRepository.addTrade(tradeUser1User2);
        tradeRepository.addTrade(tradeUser1User3);

        ConcurrentSkipListSet<Trade> user1Trades = tradeRepository.getTradeHistory("user1");
        ConcurrentSkipListSet<Trade> user2Trades = tradeRepository.getTradeHistory("user2");
        ConcurrentSkipListSet<Trade> user3Trades = tradeRepository.getTradeHistory("user3");

        assertEquals(2, user1Trades.size(), "User1 should have 2 trades.");
        assertTrue(user1Trades.contains(tradeUser1User2), "User1's history should contain trade with user2.");
        assertTrue(user1Trades.contains(tradeUser1User3), "User1's history should contain trade with user3.");

        assertEquals(1, user2Trades.size(), "User2 should have 1 trade.");
        assertTrue(user2Trades.contains(tradeUser1User2), "User2's history should contain their trade.");

        assertEquals(1, user3Trades.size(), "User3 should have 1 trade.");
        assertTrue(user3Trades.contains(tradeUser1User3), "User3's history should contain their trade.");
    }

    @Test
    void testGetTradeHistoryTradeHistoryForNonExistentUserShouldReturnEmptySet() {
        ConcurrentSkipListSet<Trade> nonExistentUserTrades = tradeRepository.getTradeHistory("user999");
        assertNotNull(nonExistentUserTrades, "Trade history for non-existent user should not be null.");
        assertTrue(nonExistentUserTrades.isEmpty(), "Trade history should be empty for a user that doesn't exist.");
    }
}
