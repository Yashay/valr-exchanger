package org.valr.service;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.valr.model.Order;
import org.valr.model.Pool;
import org.valr.model.enums.Side;
import org.valr.model.enums.TimeInForce;
import org.valr.repository.OrderBookRepository;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.valr.TestHelper.*;

class MatchingServiceTest {

    private MatchingService matchingService;
    private OrderBookRepository orderBookRepository;

    @BeforeEach
    void setUp() {
        orderBookRepository = mock(OrderBookRepository.class);
        matchingService = new MatchingService(orderBookRepository);
    }

    @Test
    void testGetPoolsForPartialOrImmediateMatchFullMatch() {
        Order takerOrder = Order.builder()
                .side(Side.BUY)
                .quantity(NUMBER(1))
                .price(NUMBER(50000))
                .timeInForce(TimeInForce.GTC)
                .build();
        Pool pool = createPool(Side.SELL, NUMBER(50000), NUMBER(1));
        ConcurrentSkipListMap<BigDecimal, Pool> sellPools = new ConcurrentSkipListMap<>();
        sellPools.put(NUMBER(50000), pool);

        when(orderBookRepository.getPoolsOppositeSide(Side.BUY)).thenReturn(sellPools);

        Pair<ConcurrentSkipListSet<Pool>, Boolean> result = matchingService.getPoolsForPartialOrImmediateMatch(takerOrder);

        assertFalse(result.getLeft().isEmpty());
        assertTrue(result.getRight());
    }

    @Test
    void testGetPoolsForPartialOrImmediateMatchPartialMatch() {
        Order takerOrder = Order.builder()
                .side(Side.BUY)
                .quantity(NUMBER(2))
                .price(NUMBER(50000))
                .timeInForce(TimeInForce.GTC)
                .build();
        Pool pool = createPool(Side.SELL, NUMBER(50000), NUMBER(1));
        ConcurrentSkipListMap<BigDecimal, Pool> sellPools = new ConcurrentSkipListMap<>();
        sellPools.put(NUMBER(50000), pool);

        when(orderBookRepository.getPoolsOppositeSide(Side.BUY)).thenReturn(sellPools);

        Pair<ConcurrentSkipListSet<Pool>, Boolean> result = matchingService.getPoolsForPartialOrImmediateMatch(takerOrder);

        assertFalse(result.getLeft().isEmpty());
        assertFalse(result.getRight());
    }

    @Test
    void testGetPoolsForPartialOrImmediateMatchNoMatch() {
        Order takerOrder = Order.builder()
                .side(Side.BUY)
                .quantity(NUMBER(1))
                .price(NUMBER(50000))
                .timeInForce(TimeInForce.GTC)
                .build();

        when(orderBookRepository.getPoolsOppositeSide(Side.BUY)).thenReturn(new ConcurrentSkipListMap<>());

        Pair<ConcurrentSkipListSet<Pool>, Boolean> result = matchingService.getPoolsForPartialOrImmediateMatch(takerOrder);

        assertTrue(result.getLeft().isEmpty());
        assertFalse(result.getRight());
    }

    @Test
    void testGetPoolsForPartialOrImmediateMatchVolumeAlreadyFilled() {
        Order takerOrder = Order.builder()
                .side(Side.BUY)
                .quantity(NUMBER(0))
                .price(NUMBER(50000))
                .timeInForce(TimeInForce.GTC)
                .build();

        when(orderBookRepository.getPoolsOppositeSide(Side.BUY))
                .thenReturn(new ConcurrentSkipListMap<>());

        Pair<ConcurrentSkipListSet<Pool>, Boolean> result = matchingService.getPoolsForPartialOrImmediateMatch(takerOrder);

        assertTrue(result.getLeft().isEmpty());
        assertTrue(result.getRight());
    }

    @Test
    void testGetPoolsForPartialOrImmediateMatchBuyPriceTooLow() {
        Order takerOrder = Order.builder()
                .side(Side.BUY)
                .quantity(NUMBER(1))
                .price(NUMBER(50000))
                .timeInForce(TimeInForce.GTC)
                .build();
        Pool pool = createPool(Side.SELL, NUMBER(51000), NUMBER(1));

        ConcurrentSkipListMap<BigDecimal, Pool> sellPools = new ConcurrentSkipListMap<>();
        sellPools.put(NUMBER(51000), pool);

        when(orderBookRepository.getPoolsOppositeSide(Side.BUY)).thenReturn(sellPools);

        Pair<ConcurrentSkipListSet<Pool>, Boolean> result = matchingService.getPoolsForPartialOrImmediateMatch(takerOrder);

        assertTrue(result.getLeft().isEmpty());
        assertFalse(result.getRight());
    }

    @Test
    void testGetPoolsForPartialOrImmediateMatchSellPriceTooHigh() {
        Order takerOrder = Order.builder()
                .side(Side.SELL)
                .quantity(NUMBER(1))
                .price(NUMBER(50000))
                .timeInForce(TimeInForce.GTC)
                .build();
        Pool pool = createPool(Side.BUY, NUMBER(49000), NUMBER(1));

        ConcurrentSkipListMap<BigDecimal, Pool> buyPools = new ConcurrentSkipListMap<>();
        buyPools.put(NUMBER(49000), pool);

        when(orderBookRepository.getPoolsOppositeSide(Side.SELL)).thenReturn(buyPools);

        Pair<ConcurrentSkipListSet<Pool>, Boolean> result = matchingService.getPoolsForPartialOrImmediateMatch(takerOrder);

        assertTrue(result.getLeft().isEmpty());
        assertFalse(result.getRight());
    }

    @Test
    void testGetPoolsForPartialOrImmediateMatchTakerBuyFullMatchThreeSellPools() {
        Order takerOrder = Order.builder()
                .side(Side.BUY)
                .quantity(NUMBER(20))
                .price(NUMBER(52000))
                .timeInForce(TimeInForce.GTC)
                .build();
        Pool pool1 = createPool(Side.SELL, NUMBER(50000), NUMBER(10));
        Pool pool2 = createPool(Side.SELL, NUMBER(51000), NUMBER(10));
        Pool pool3 = createPool(Side.SELL, NUMBER(52000), NUMBER(10));

        ConcurrentSkipListMap<BigDecimal, Pool> sellPools = new ConcurrentSkipListMap<>();
        sellPools.put(NUMBER(50000), pool1);
        sellPools.put(NUMBER(51000), pool2);
        sellPools.put(NUMBER(52000), pool3);

        when(orderBookRepository.getPoolsOppositeSide(Side.BUY)).thenReturn(sellPools);

        Pair<ConcurrentSkipListSet<Pool>, Boolean> result = matchingService.getPoolsForPartialOrImmediateMatch(takerOrder);

        assertEquals(2, result.getLeft().size());
        assertTrue(result.getRight());
    }

}
