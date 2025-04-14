package org.valr.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.valr.model.enums.ExchangePair;
import org.valr.model.enums.Side;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.valr.TestHelper.*;

class TradeTest {

    private Order takerOrder;
    private Order makerOrder;

    @BeforeEach
    void setUp() {
        takerOrder = Order.builder()
                .userId("takerId")
                .side(Side.BUY)
                .exchangePair(ExchangePair.BTCZAR)
                .quantity(NUMBER(1))
                .price(NUMBER(50000))
                .build();
        makerOrder = Order.builder()
                .userId("makerId")
                .side(Side.SELL)
                .exchangePair(ExchangePair.BTCZAR)
                .quantity(NUMBER(1))
                .price(NUMBER(50000))
                .build();
    }

    @Test
    void testTradeInitialization() {
        Trade trade = new Trade(takerOrder, makerOrder, NUMBER(50000), NUMBER(1));

        assertNotNull(trade.getTradeId());
        assertEquals(NUMBER(50000), trade.getPrice());
        assertEquals(NUMBER(1), trade.getQuantity());
        assertEquals(NUMBER(50000), trade.getQuoteVolume());
        assertEquals(Side.BUY, trade.getTakerSide());
        assertEquals(Side.SELL, trade.getMakerSide());
        assertEquals("takerId", trade.getTakerId());
        assertEquals("makerId", trade.getMakerId());
        assertEquals(takerOrder.getOrderId(), trade.getTakerOrderId());
        assertEquals(makerOrder.getOrderId(), trade.getMakerOrderId());
        assertEquals(ExchangePair.BTCZAR, trade.getCurrenyExchangePair());
        assertNotNull(trade.getTradedAt());
    }

    @Test
    void testTradeComparison() {
        Trade trade1 = new Trade(takerOrder, makerOrder, NUMBER(50000), NUMBER(1), Instant.ofEpochSecond(10));
        Trade trade2 = new Trade(takerOrder, makerOrder, NUMBER(50000), NUMBER(1), Instant.ofEpochSecond(20));

        assertTrue(trade1.compareTo(trade2) < 0 || trade1.compareTo(trade2) > 0);

        Trade laterTrade = new Trade(takerOrder, makerOrder, NUMBER(50000), NUMBER(1), Instant.ofEpochSecond(30));

        assertTrue(trade1.compareTo(laterTrade) < 0);
    }

    @Test
    void testTradeEquality() {
        Trade trade1 = new Trade(takerOrder, makerOrder, NUMBER(50000), NUMBER(1), Instant.ofEpochSecond(10));
        Trade trade2 = new Trade(takerOrder, makerOrder, NUMBER(50000), NUMBER(1), Instant.ofEpochSecond(20));

        assertNotEquals(trade1, trade2);
    }

    @Test
    void testInvalidTrade() {
        assertThrows(NullPointerException.class, () -> new Trade(null, makerOrder, NUMBER(50000), NUMBER(1)));
        assertThrows(NullPointerException.class, () -> new Trade(takerOrder, null, NUMBER(50000), NUMBER(1)));
    }
}
