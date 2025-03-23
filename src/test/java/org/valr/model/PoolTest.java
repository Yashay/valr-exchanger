package org.valr.model;

import org.junit.jupiter.api.Test;
import org.valr.model.enums.ExchangePair;
import org.valr.model.enums.Side;

import static org.junit.jupiter.api.Assertions.*;
import static org.valr.TestHelper.*;

class PoolTest {

    @Test
    void testPoolInitialization() {
        Pool pool = new Pool(Side.BUY, ExchangePair.BTCZAR, NUMBER(50000));
        assertEquals(Side.BUY, pool.getSide());
        assertEquals(ExchangePair.BTCZAR, pool.getCurrencyPair());
        assertEquals(NUMBER(50000), pool.getPrice());
        assertEquals(NUMBER(0), pool.getVolume().get());
        assertTrue(pool.getOrders().isEmpty());
    }

    @Test
    void testAddOrder() {
        Pool pool = new Pool(Side.SELL, ExchangePair.BTCZAR, NUMBER(50000));
        Order order = createOrder(Side.SELL, NUMBER(50000), NUMBER(1));

        pool.addOrder(order);

        assertEquals(NUMBER(1), pool.getVolume().get());
        assertTrue(pool.getOrders().contains(order));
    }

    @Test
    void testRemoveOrder() {
        Pool pool = new Pool(Side.SELL, ExchangePair.BTCZAR, NUMBER(50000));
        Order order = createOrder(Side.SELL, NUMBER(50000), NUMBER(1));
        pool.addOrder(order);

        pool.removeOrder(order);

        assertEquals(NUMBER(0), pool.getVolume().get());
        assertFalse(pool.getOrders().contains(order));
    }

    @Test
    void testReduceQuantity() {
        Pool pool = new Pool(Side.SELL, ExchangePair.BTCZAR, NUMBER(50000));
        pool.getVolume().set(NUMBER(10));

        pool.reduceQuantity(NUMBER(3));

        assertEquals(NUMBER(7), pool.getVolume().get());
    }

    @Test
    void testCompareTo() {
        Pool pool1 = new Pool(Side.BUY, ExchangePair.BTCZAR, NUMBER(50000));
        Pool pool2 = new Pool(Side.BUY, ExchangePair.BTCZAR, NUMBER(60000));

        assertTrue(pool1.compareTo(pool2) < 0);
        assertTrue(pool2.compareTo(pool1) > 0);
        assertEquals(0, pool1.compareTo(new Pool(Side.BUY, ExchangePair.BTCZAR, NUMBER(50000))));
    }

    @Test
    void testDefaultConstructor() {
        Pool pool = new Pool(NUMBER(50000));
        assertEquals(NUMBER(50000), pool.getPrice());
        assertNull(pool.getSide());
        assertNull(pool.getCurrencyPair());
        assertEquals(NUMBER(0), pool.getVolume().get());
        assertTrue(pool.getOrders().isEmpty());
    }
}
