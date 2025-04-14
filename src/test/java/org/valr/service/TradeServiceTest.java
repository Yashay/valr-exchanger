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
import org.valr.model.enums.ExchangePair;
import org.valr.model.enums.Side;
import org.valr.repository.OrderBookRepository;
import org.valr.repository.TradeRepository;

import java.time.Instant;
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
        Order takerOrder = Order.builder()
                .side(Side.BUY)
                .exchangePair(ExchangePair.BTCZAR)
                .quantity(NUMBER(2))
                .price(NUMBER(50000))
                .timestamp(Instant.ofEpochMilli(2000))
                .sequence(1L)
                .build();
        Order makerOrder = Order.builder()
                .side(Side.SELL)
                .exchangePair(ExchangePair.BTCZAR)
                .quantity(NUMBER(2))
                .price(NUMBER(50000))
                .timestamp(Instant.ofEpochMilli(3000))
                .sequence(2L)
                .build();
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
        Order takerOrder = Order.builder()
                .side(Side.BUY)
                .exchangePair(ExchangePair.BTCZAR)
                .quantity(NUMBER(3))
                .price(NUMBER(50000))
                .timestamp(Instant.ofEpochMilli(2000))
                .sequence(1L)
                .build();
        Order makerOrder = Order.builder()
                .side(Side.SELL)
                .exchangePair(ExchangePair.BTCZAR)
                .quantity(NUMBER(2))
                .price(NUMBER(50000))
                .timestamp(Instant.ofEpochMilli(3000))
                .sequence(2L)
                .build();
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
        Order takerOrder = Order.builder()
                .side(Side.BUY)
                .exchangePair(ExchangePair.BTCZAR)
                .quantity(NUMBER(1))
                .price(NUMBER(50000))
                .timestamp(Instant.ofEpochMilli(2000))
                .sequence(1L)
                .build();
        Order makerOrder = Order.builder()
                .side(Side.SELL)
                .exchangePair(ExchangePair.BTCZAR)
                .quantity(NUMBER(1))
                .price(NUMBER(50000))
                .timestamp(Instant.ofEpochMilli(3000))
                .sequence(2L)
                .build();
        Trade trade = new Trade(takerOrder, makerOrder, NUMBER(1), NUMBER(50000), Instant.now());
        expectedTrades.add(TradeDTO.from(trade).toJson());

        when(tradeRepository.getTradeHistory(userId)).thenReturn(new ConcurrentSkipListSet<>(Set.of(trade)));

        JsonArray result = tradeService.getRecentTradeHistory(userId);

        assertEquals(expectedTrades.size(), result.size());
        assertTrue(expectedTrades.getValue(0).toString().equalsIgnoreCase(result.getValue(0).toString()));
    }
}
