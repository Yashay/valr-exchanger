package org.valr.service;

import io.vertx.core.json.Json;
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
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.valr.TestHelper.*;

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
    void testGetOrderBookSnapshotEmpty() {
        when(orderBookRepository.getSellPoolMap()).thenReturn(new ConcurrentSkipListMap<BigDecimal, Pool>());
        when(orderBookRepository.getBuyPoolMap()).thenReturn(new ConcurrentSkipListMap<BigDecimal, Pool>());
        when(orderBookRepository.getOrderCounter()).thenReturn(new AtomicLong(42));

        JsonObject snapshot = orderBookService.getOrderBookSnapshot("BTCZAR");

        assertNotNull(snapshot);
        assertEquals(0, snapshot.getJsonArray("Asks").size());
        assertEquals(0, snapshot.getJsonArray("Bids").size());
        assertEquals(42L, snapshot.getLong("sequence"));
        assertNotNull(snapshot.getInstant("createdAt"));
    }

    @Test
    void testGetOrderBookSnapshotWithOrders() {

        ConcurrentSkipListMap<BigDecimal, Pool> sellPoolMap = new ConcurrentSkipListMap<>();
        sellPoolMap.put(NUMBER(500), createPool(Side.BUY, NUMBER(50000), NUMBER(10)));
        ConcurrentSkipListMap<BigDecimal, Pool> buyPoolMap = new ConcurrentSkipListMap<>();
        buyPoolMap.put(NUMBER(500), createPool(Side.BUY, NUMBER(40000), NUMBER(10)));

        when(orderBookRepository.getSellPoolMap()).thenReturn(sellPoolMap);
        when(orderBookRepository.getBuyPoolMap()).thenReturn(buyPoolMap);
        when(orderBookRepository.getOrderCounter()).thenReturn(new AtomicLong(42));

        JsonObject snapshot = orderBookService.getOrderBookSnapshot("BTCZAR");

        String asksPrice = snapshot.getJsonArray("Asks").getJsonObject(0).getString("price");
        String bidsPrice = snapshot.getJsonArray("Bids").getJsonObject(0).getString("price");

        assertNotNull(snapshot);
        assertEquals(1, snapshot.getJsonArray("Asks").size());
        assertEquals(1, snapshot.getJsonArray("Bids").size());
        assertEquals(NUMBER(50000), NUMBER(asksPrice));
        assertEquals(NUMBER(40000), NUMBER(bidsPrice));
        assertEquals(42L, snapshot.getLong("sequence"));
        assertNotNull(snapshot.getInstant("createdAt"));
    }
}
