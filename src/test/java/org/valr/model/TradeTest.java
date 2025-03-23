package org.valr.model;

import org.junit.jupiter.api.Test;
import org.valr.model.enums.ExchangePair;
import org.valr.model.enums.Side;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.valr.TestHelper.*;

class TradeTest {

    @Test
    void testTradeInitialization() {
        Order takerOrder = createOrder("takerId", Side.BUY, NUMBER(50000), NUMBER(1));
        Order makerOrder = createOrder("makerId", Side.SELL, NUMBER(50000), NUMBER(1));

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
        Trade trade1 = new Trade(createOrder(Side.BUY, NUMBER(50000), NUMBER(1)), createOrder(Side.SELL, NUMBER(50000), NUMBER(1)), NUMBER(50000), NUMBER(1), Instant.ofEpochSecond(10));
        Trade trade2 = new Trade(createOrder(Side.BUY, NUMBER(50000), NUMBER(1)), createOrder(Side.SELL, NUMBER(50000), NUMBER(1)), NUMBER(50000), NUMBER(1), Instant.ofEpochSecond(20));

        assertTrue(trade1.compareTo(trade2) < 0 || trade1.compareTo(trade2) > 0);

        Trade laterTrade = new Trade(createOrder(Side.BUY, NUMBER(50000), NUMBER(1)), createOrder(Side.SELL, NUMBER(50000), NUMBER(1)), NUMBER(50000), NUMBER(1), Instant.ofEpochSecond(30));

        assertTrue(trade1.compareTo(laterTrade) < 0);
    }

    @Test
    void testTradeEquality() {
        Trade trade1 = new Trade(createOrder(Side.BUY, NUMBER(50000), NUMBER(1)), createOrder(Side.SELL, NUMBER(50000), NUMBER(1)), NUMBER(50000), NUMBER(1));
        Trade trade2 = new Trade(createOrder(Side.BUY, NUMBER(50000), NUMBER(1)), createOrder(Side.SELL, NUMBER(50000), NUMBER(1)), NUMBER(50000), NUMBER(1));

        assertNotEquals(trade1, trade2);
    }

    @Test
    void testInvalidTrade() {
        Order takerOrder = createOrder("takerId", Side.BUY, NUMBER(50000), NUMBER(1));
        Order makerOrder = createOrder("makerId", Side.SELL, NUMBER(50000), NUMBER(1));

        assertThrows(NullPointerException.class, () -> new Trade(null, makerOrder, NUMBER(50000), NUMBER(1)));
        assertThrows(NullPointerException.class, () -> new Trade(takerOrder, null, NUMBER(50000), NUMBER(1)));
    }
}
