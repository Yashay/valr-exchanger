package org.valr;

import org.valr.model.Order;
import org.valr.model.Pool;
import org.valr.model.Trade;
import org.valr.model.enums.ExchangePair;
import org.valr.model.enums.Side;
import org.valr.model.enums.TimeInForce;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class TestHelper {

    private static final AtomicLong counter = new AtomicLong(0);

    public static BigDecimal NUMBER(int number) {
        return BigDecimal.valueOf(number);
    }

    public static BigDecimal NUMBER(String number) {
        return new BigDecimal(number);
    }

    // TODO this needs some refactoring from here on
    public static Order createOrder(String userId, Side side, BigDecimal price, BigDecimal quantity, TimeInForce timeInForce, Instant timeCreated, long sequence) {
        Order order = new Order();
        order.setUserId(userId);
        order.setSide(side);
        order.setExchangePair(ExchangePair.BTCZAR);
        order.setPrice(price);
        order.setInitialQuantity(quantity);
        order.setQuantity(quantity);
        order.setTimeInForce(timeInForce);
        order.setTimestamp(timeCreated);
        order.setSequence(sequence);
        return order;
    }

    public static Order createOrder(Side side, BigDecimal price, BigDecimal quantity, long timeCreated) {
        return createOrder("userId", side, price, quantity, TimeInForce.GTC, Instant.ofEpochSecond(timeCreated), counter.incrementAndGet());
    }

    public static Order createOrder(Side side, BigDecimal price, BigDecimal quantity) {
        return createOrder("userId", side, price, quantity, TimeInForce.GTC, Instant.now(), counter.incrementAndGet());
    }

    public static Order createOrder(Side side, BigDecimal price, BigDecimal quantity, TimeInForce timeInForce) {
        return createOrder("userId", side, price, quantity, timeInForce, Instant.now(), counter.incrementAndGet());
    }

    public static Order createOrder(String userId, Side side, BigDecimal price, BigDecimal quantity) {
        return createOrder(userId, side, price, quantity, TimeInForce.GTC, Instant.now(), counter.incrementAndGet());
    }

    public static Order createOrder(String userId, Side side, BigDecimal price, BigDecimal quantity, TimeInForce timeInForce) {
        return createOrder(userId, side, price, quantity, timeInForce, Instant.now(), counter.incrementAndGet());
    }

    public static Order createOrder(String userId, Side side, BigDecimal price, BigDecimal quantity, TimeInForce timeInForce, long sequence) {
        return createOrder(userId, side, price, quantity, timeInForce, Instant.now(), sequence);
    }

    public static Order createOrder(String userId, Side side, BigDecimal price, BigDecimal quantity, TimeInForce timeInForce, Instant timeCreated) {
        return createOrder(userId, side, price, quantity, timeInForce, timeCreated, counter.incrementAndGet());
    }

    public static Pool createPool(Order makerOrder) {
        Pool pool = new Pool(makerOrder.getSide(), makerOrder.getExchangePair(), makerOrder.getPrice());
        pool.getOrders().add(makerOrder);
        return pool;
    }

    public static Pool createPool(Side side, BigDecimal price, BigDecimal volume) {
        Pool pool = new Pool(side, ExchangePair.BTCZAR, price);
        pool.getVolume().updateAndGet(v -> v.add(volume));
        return pool;
    }

    public static Trade createTrade(Side side, BigDecimal price, BigDecimal quantity) {
        Order takerOrder = createOrder(side, price, quantity);
        Order makerOrder = createOrder(side == Side.BUY ? Side.SELL : Side.BUY, price, quantity);
        return new Trade(takerOrder, makerOrder, price, quantity);
    }
}
