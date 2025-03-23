package org.valr.service;

import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.valr.model.Order;
import org.valr.model.Pool;
import org.valr.model.enums.Side;
import org.valr.model.enums.TimeInForce;
import org.valr.repository.OrderBookRepository;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.valr.TestHelper.NUMBER;
import static org.valr.TestHelper.createOrder;

class OrderBookServiceTest {

    @InjectMocks
    private OrderBookService orderBookService;

    @Mock
    private OrderBookRepository orderBookRepository;

    @Mock
    private BalanceService balanceService;

    @Mock
    private TradeService tradeService;

    @Mock
    private MatchingService matchingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPlaceLimitOrderFOKSuccess() {
        Order order = createOrder(Side.BUY, NUMBER(50000), NUMBER(1), TimeInForce.FOK);
        when(balanceService.reserveOnOrder(order)).thenReturn(true);
        when(matchingService.getPoolsForPartialOrImmediateMatch(order))
                .thenReturn(Pair.of(new ConcurrentSkipListSet<>(), true));

        JsonObject response = orderBookService.placeLimitOrder("userId", order);

        verify(balanceService).reserveOnOrder(order);
        verify(tradeService).executeMatches(eq(order), any());
        verify(orderBookRepository, never()).removeOrder(order);
        assertTrue(response.isEmpty());
    }

    @Test
    void testPlaceLimitOrderFOKFailNotFillable() {
        Order order = createOrder(Side.BUY, NUMBER(50000), NUMBER(1), TimeInForce.FOK);
        when(balanceService.reserveOnOrder(order)).thenReturn(true);
        when(matchingService.getPoolsForPartialOrImmediateMatch(order))
                .thenReturn(Pair.of(new ConcurrentSkipListSet<>(), false));

        JsonObject response = orderBookService.placeLimitOrder("userId", order);

        assertTrue(response.isEmpty());
    }

    @Test
    void testPlaceLimitOrderIOCSuccess() {
        Order order = createOrder(Side.BUY, NUMBER(50000), NUMBER(1), TimeInForce.IOC);
        when(balanceService.reserveOnOrder(order)).thenReturn(true);
        when(matchingService.getPoolsForPartialOrImmediateMatch(order))
                .thenReturn(Pair.of(new ConcurrentSkipListSet<>(), true));

        JsonObject response = orderBookService.placeLimitOrder("userId", order);

        verify(tradeService).executeMatches(eq(order), any());
        verify(balanceService).unreserveOnOrder(order);
        assertNotNull(response);
    }

    @Test
    void testPlaceLimitOrderGTCSuccess() {
        Order order = createOrder(Side.BUY, NUMBER(50000), NUMBER(1), TimeInForce.GTC);
        when(balanceService.reserveOnOrder(order)).thenReturn(true);
        when(matchingService.getPoolsForPartialOrImmediateMatch(order))
                .thenReturn(Pair.of(new ConcurrentSkipListSet<>(), true));

        JsonObject response = orderBookService.placeLimitOrder("userId", order);

        verify(tradeService).executeMatches(eq(order), any());
        verify(orderBookRepository).addOrder(order);
        assertNotNull(response);
    }

    @Test
    void testPlaceLimitOrderInsufficientFunds() {
        Order order = createOrder(Side.BUY, NUMBER(50000), NUMBER(1), TimeInForce.FOK);
        when(balanceService.reserveOnOrder(order)).thenReturn(false);

        JsonObject response = orderBookService.placeLimitOrder("userId", order);

        assertFalse(response.getBoolean("placed"));
        assertEquals("Insufficient funds", response.getString("message"));
    }

    @Test
    void testGetOrderBookSnapshot() {
        when(orderBookRepository.getSellPoolMap()).thenReturn(new ConcurrentSkipListMap<BigDecimal, Pool>());
        when(orderBookRepository.getBuyPoolMap()).thenReturn(new ConcurrentSkipListMap<BigDecimal, Pool>());
        when(orderBookRepository.getOrderCounter()).thenReturn(new java.util.concurrent.atomic.AtomicLong(42));

        JsonObject snapshot = orderBookService.getOrderBookSnapshot("BTCZAR");

        assertNotNull(snapshot);
        assertEquals(0, snapshot.getJsonArray("Asks").size());
        assertEquals(0, snapshot.getJsonArray("Bids").size());
        assertEquals(42L, snapshot.getLong("sequence"));
        assertNotNull(snapshot.getInstant("createdAt"));
    }
}
