package org.valr;

import org.valr.model.Order;
import org.valr.model.enums.ExchangePair;
import org.valr.model.enums.Side;
import org.valr.model.enums.TimeInForce;

import java.math.BigDecimal;
import java.time.Instant;

public class TestHelper {

    public static BigDecimal NUMBER(int number) {
        return BigDecimal.valueOf(number);
    }

    public static Order createOrder(String userId, Side side, BigDecimal price, BigDecimal quantity) {
        Order order = new Order();
        order.setUserId(userId);
        order.setSide(side);
        order.setExchangePair(ExchangePair.BTCZAR);
        order.setPrice(price);
        order.setInitialQuantity(quantity);
        order.setQuantity(quantity);
        order.setTimeInForce(TimeInForce.FOK);
        order.setTimestamp(Instant.now());
        return order;
    }

}
