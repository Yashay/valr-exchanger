package org.valr.service;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.valr.model.Order;
import org.valr.model.enums.TimeInForce;
import org.valr.repository.OrderBookRepository;

import java.util.concurrent.ConcurrentSkipListSet;

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
        Order order = Order.builder()
                .quantity(NUMBER(1))
                .price(NUMBER(50000))
                .timeInForce(TimeInForce.FOK)
                .build();

        when(balanceService.reserveOnOrder(order)).thenReturn(true);
        when(matchingService.getPoolsForPartialOrImmediateMatch(order))
                .thenReturn(Pair.of(new ConcurrentSkipListSet<>(), true));

        placementService.placeLimitOrder(order);

        verify(tradeService).executeMatches(eq(order), any());
        verify(orderBookRepository, never()).removeOrder(order);
    }

    @Test
    void testPlaceLimitOrderFOKFailNotFillable() {
        Order order = Order.builder()
                .quantity(NUMBER(1))
                .price(NUMBER(50000))
                .timeInForce(TimeInForce.FOK)
                .build();

        when(balanceService.reserveOnOrder(order)).thenReturn(true);
        when(matchingService.getPoolsForPartialOrImmediateMatch(order))
                .thenReturn(Pair.of(new ConcurrentSkipListSet<>(), false));

        placementService.placeLimitOrder(order);

        verify(balanceService).unreserveOnOrder(order);
    }

    @Test
    void testPlaceLimitOrderIOCSuccess() {
        Order order = Order.builder()
                .quantity(NUMBER(1))
                .price(NUMBER(50000))
                .timeInForce(TimeInForce.IOC)
                .build();

        when(balanceService.reserveOnOrder(order)).thenReturn(true);
        when(matchingService.getPoolsForPartialOrImmediateMatch(order))
                .thenReturn(Pair.of(new ConcurrentSkipListSet<>(), true));

        placementService.placeLimitOrder(order);

        verify(tradeService).executeMatches(eq(order), any());
        verify(balanceService).unreserveOnOrder(order);
    }

    @Test
    void testPlaceLimitOrderGTCSuccess() {
        Order order = Order.builder()
                .quantity(NUMBER(1))
                .price(NUMBER(50000))
                .timeInForce(TimeInForce.GTC)
                .build();

        when(balanceService.reserveOnOrder(order)).thenReturn(true);
        when(matchingService.getPoolsForPartialOrImmediateMatch(order))
                .thenReturn(Pair.of(new ConcurrentSkipListSet<>(), true));

        placementService.placeLimitOrder(order);

        verify(tradeService).executeMatches(eq(order), any());
        verify(orderBookRepository).addOrder(order);
    }
}
