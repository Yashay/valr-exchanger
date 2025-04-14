package org.valr.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.valr.model.Order;
import org.valr.model.Pool;
import org.valr.model.enums.Side;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.valr.TestHelper.NUMBER;

class OrderBookRepositoryTest {

    private OrderBookRepository orderBookRepository;
    private Order sellOrder;
    private Order buyOrder;
    private Order buyOrder2;

    @BeforeEach
    void setUp() {
        orderBookRepository = new OrderBookRepository();
        buyOrder = Order.builder()
                .side(Side.BUY)
                .quantity(NUMBER(10))
                .price(NUMBER(50000))
                .timestamp(Instant.now())
                .build();
        sellOrder = Order.builder()
                .side(Side.SELL)
                .quantity(NUMBER(10))
                .price(NUMBER(50000))
                .timestamp(Instant.now())
                .build();
        buyOrder2 = Order.builder()
                .side(Side.BUY)
                .quantity(NUMBER(10))
                .price(NUMBER(50000))
                .timestamp(Instant.now())
                .build();
    }

    @Test
    void testAddOrderBuyOrderShouldBeAddedToBuyPool() {
        orderBookRepository.addOrder(buyOrder);

        Pool buyPool = orderBookRepository.getPoolsMapBySide(Side.BUY).get(buyOrder.getPrice());
        assertNotNull(buyPool, "Buy pool should contain the order.");
        assertTrue(buyPool.getOrders().contains(buyOrder), "Buy pool should have the added order.");
    }

    @Test
    void testAddOrderSellOrderShouldBeAddedToSellPool() {
        orderBookRepository.addOrder(sellOrder);

        Pool sellPool = orderBookRepository.getPoolsMapBySide(Side.SELL).get(sellOrder.getPrice());
        assertNotNull(sellPool, "Sell pool should contain the order.");
        assertTrue(sellPool.getOrders().contains(sellOrder), "Sell pool should have the added order.");
    }

    @Test
    void testRemoveOrderOrderExistsShouldBeRemoved() {
        orderBookRepository.addOrder(buyOrder);
        orderBookRepository.removeOrder(buyOrder);

        Pool buyPool = orderBookRepository.getPoolsMapBySide(Side.BUY).get(buyOrder.getPrice());
        assertNull(buyPool, "Buy pool should not contain the order after removal.");
    }

    @Test
    void testRemoveOrderOrderNotInPoolShouldNotCauseError() {
        orderBookRepository.removeOrder(sellOrder);

        Pool sellPool = orderBookRepository.getPoolsMapBySide(Side.SELL).get(sellOrder.getPrice());
        assertNull(sellPool, "Sell pool should not contain the order.");
    }
    
    @Test
    void testGetPoolByOrderShouldReturnCorrectPool() {
        orderBookRepository.addOrder(buyOrder);

        Optional<Pool> poolOptional = orderBookRepository.getPoolByOrder(buyOrder);

        assertTrue(poolOptional.isPresent(), "Pool should be present.");
        assertEquals(buyOrder.getPrice(), poolOptional.get().getPrice(), "Pool should correspond to the order price.");
    }

    @Test
    void testGetPoolsOppositeSideShouldReturnCorrectPool() {
        orderBookRepository.addOrder(buyOrder);
        orderBookRepository.addOrder(sellOrder);

        ConcurrentSkipListMap<BigDecimal, Pool> oppositeSidePools = orderBookRepository.getPoolsOppositeSide(Side.BUY);

        assertEquals(1, oppositeSidePools.size(), "Sell pool should be present in the opposite sell map.");
        assertTrue(oppositeSidePools.get(sellOrder.getPrice()).getOrders().contains(sellOrder), "Sell order should be present in the sell pool");
    }

    @Test
    void testRemoveOrderWhenPoolIsEmptyShouldRemovePool() {
        orderBookRepository.addOrder(buyOrder);
        orderBookRepository.removeOrder(buyOrder);

        Pool buyPool = orderBookRepository.getPoolsMapBySide(Side.BUY).get(buyOrder.getPrice());

        assertNull(buyPool);
    }

    @Test
    void testRemoveOrderWhenOrderIsLastInPoolShouldRemovePool() {
        orderBookRepository.addOrder(buyOrder);
        orderBookRepository.addOrder(buyOrder2);
        orderBookRepository.removeOrder(buyOrder);

        Pool buyPool = orderBookRepository.getPoolsMapBySide(Side.BUY).get(buyOrder.getPrice());

        assertNotNull(buyPool);

        orderBookRepository.removeOrder(buyOrder2);

        buyPool = orderBookRepository.getPoolsMapBySide(Side.BUY).get(buyOrder2.getPrice());

        assertNull(buyPool);
    }
}
