package org.valr.service;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.valr.model.Pool;
import org.valr.model.enums.Side;
import org.valr.repository.OrderBookRepository;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.valr.TestHelper.*;

class OrderBookServiceTest {

    @InjectMocks
    private OrderBookService orderBookService;

    @Mock
    private OrderBookRepository orderBookRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
