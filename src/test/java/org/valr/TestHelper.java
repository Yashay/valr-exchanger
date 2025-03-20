package org.valr;

import org.valr.model.Order;
import org.valr.model.Pool;
import org.valr.model.Trade;
import org.valr.model.enums.ExchangePair;
import org.valr.model.enums.Side;
import org.valr.model.enums.TimeInForce;

import java.math.BigDecimal;
import java.time.Instant;

public class TestHelper {

    public static BigDecimal NUMBER(int number) {
        return BigDecimal.valueOf(number);
    }

    public static Order createOrder(Side side, BigDecimal price, BigDecimal quantity) {
        return createOrder("userId", side, price, quantity, TimeInForce.GTC);
    }

    public static Order createOrder(Side side, BigDecimal price, BigDecimal quantity, TimeInForce timeInForce) {
        return createOrder("userId", side, price, quantity, timeInForce);
    }

    public static Order createOrder(String userId, Side side, BigDecimal price, BigDecimal quantity) {
        return createOrder(userId, side, price, quantity, TimeInForce.GTC);
    }

    public static Order createOrder(String userId, Side side, BigDecimal price, BigDecimal quantity, TimeInForce timeInForce) {
        Order order = new Order();
        order.setUserId(userId);
        order.setSide(side);
        order.setExchangePair(ExchangePair.BTCZAR);
        order.setPrice(price);
        order.setInitialQuantity(quantity);
        order.setQuantity(quantity);
        order.setTimeInForce(timeInForce);
        order.setTimestamp(Instant.now());
        order.setSequence(Instant.now().toEpochMilli());
        return order;
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
