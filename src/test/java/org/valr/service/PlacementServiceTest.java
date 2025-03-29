package org.valr.service;

import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.valr.model.Order;
import org.valr.model.enums.Side;
import org.valr.model.enums.TimeInForce;
import org.valr.repository.OrderBookRepository;

import java.util.concurrent.ConcurrentSkipListSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.valr.TestHelper.*;

class PlacementServiceTest {

    @InjectMocks
    private PlacementService placementService;

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

        JsonObject response = placementService.placeLimitOrder(order);

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

        JsonObject response = placementService.placeLimitOrder(order);

        assertTrue(response.isEmpty());
    }

    @Test
    void testPlaceLimitOrderIOCSuccess() {
        Order order = createOrder(Side.BUY, NUMBER(50000), NUMBER(1), TimeInForce.IOC);
        when(balanceService.reserveOnOrder(order)).thenReturn(true);
        when(matchingService.getPoolsForPartialOrImmediateMatch(order))
                .thenReturn(Pair.of(new ConcurrentSkipListSet<>(), true));

        JsonObject response = placementService.placeLimitOrder(order);

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

        JsonObject response = placementService.placeLimitOrder(order);

        verify(tradeService).executeMatches(eq(order), any());
        verify(orderBookRepository).addOrder(order);
        assertNotNull(response);
    }

    @Test
    void testPlaceLimitOrderInsufficientFunds() {
        Order order = createOrder(Side.BUY, NUMBER(50000), NUMBER(1), TimeInForce.FOK);
        when(balanceService.reserveOnOrder(order)).thenReturn(false);

        JsonObject response = placementService.placeLimitOrder(order);

        assertFalse(response.getBoolean("placed"));
        assertEquals("Insufficient funds", response.getString("message"));
    }
}
