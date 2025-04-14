package org.valr;

import org.valr.model.Order;
import org.valr.model.Pool;
import org.valr.model.enums.ExchangePair;
import org.valr.model.enums.Side;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

public class TestHelper {

    private static final AtomicLong counter = new AtomicLong(0);

    public static BigDecimal NUMBER(int number) {
        return BigDecimal.valueOf(number);
    }

    public static BigDecimal NUMBER(String number) {
        return new BigDecimal(number);
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
}
