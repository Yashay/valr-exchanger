package org.valr.service;

import io.vertx.core.json.JsonArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.valr.dto.TradeDTO;
import org.valr.model.Order;
import org.valr.model.Pool;
import org.valr.model.Trade;
import org.valr.model.enums.Side;
import org.valr.repository.OrderBookRepository;
import org.valr.repository.TradeRepository;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.valr.TestHelper.*;

class TradeServiceTest {

    @InjectMocks
    private TradeService tradeService;

    @Mock
    private BalanceService balanceService;

    @Mock
    private OrderBookRepository orderBookRepository;

    @Mock
    private TradeRepository tradeRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExecuteMatchesFullMatch() {
        Order takerOrder = createOrder(Side.BUY, NUMBER(50000), NUMBER(2));
        Order makerOrder = createOrder(Side.SELL, NUMBER(50000), NUMBER(2));
        Pool pool = createPool(makerOrder);
        ConcurrentSkipListSet<Pool> validVolumePools = new ConcurrentSkipListSet<>();
        validVolumePools.add(pool);

        tradeService.executeMatches(takerOrder, validVolumePools);

        verify(balanceService).adjustBalancesForTrade(takerOrder, makerOrder, NUMBER(2));
        verify(orderBookRepository).removeOrder(takerOrder);
        verify(orderBookRepository).removeOrder(makerOrder);
        verify(tradeRepository).addTrade(any(Trade.class));
    }

    @Test
    void testExecuteMatchesPartialMatch() {
        Order takerOrder = createOrder(Side.BUY, NUMBER(50000), NUMBER(3));
        Order makerOrder = createOrder(Side.SELL, NUMBER(50000), NUMBER(2));
        Pool pool = createPool(makerOrder);
        ConcurrentSkipListSet<Pool> validVolumePools = new ConcurrentSkipListSet<>();
        validVolumePools.add(pool);

        tradeService.executeMatches(takerOrder, validVolumePools);

        verify(balanceService).adjustBalancesForTrade(takerOrder, makerOrder, NUMBER(2));
        verify(orderBookRepository).removeOrder(makerOrder);
        verify(tradeRepository).addTrade(any(Trade.class));
    }

    @Test
    void testGetRecentTradeHistory() {
        String userId = "userId";
        JsonArray expectedTrades = new JsonArray();
        Trade trade = createTrade(Side.BUY, NUMBER(50000), NUMBER(1));
        expectedTrades.add(TradeDTO.from(trade).toJson());
        when(tradeRepository.getTradeHistory(userId)).thenReturn(new ConcurrentSkipListSet<>(Set.of(trade)));

        JsonArray result = tradeService.getRecentTradeHistory(userId);

        assertEquals(expectedTrades.size(), result.size());
        assertTrue(expectedTrades.getValue(0).toString().equalsIgnoreCase(result.getValue(0).toString()));
    }
}
