package org.valr.repository;

import lombok.Getter;
import lombok.Setter;
import org.valr.model.Order;
import org.valr.model.Pool;
import org.valr.model.enums.Side;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@Setter
public class OrderBookRepository {
    private final ConcurrentSkipListMap<BigDecimal, Pool> buyPoolMap = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
    private final ConcurrentSkipListMap<BigDecimal, Pool> sellPoolMap = new ConcurrentSkipListMap<>();
    private final AtomicLong orderCounter = new AtomicLong();

    public OrderBookRepository() {}

    public void addOrder(Order order) {
        order.setSequence(orderCounter.getAndIncrement());
        getPoolsMapBySide(order.getSide())
                .computeIfAbsent(order.getPrice(), p -> {
                    Pool pool = new Pool(order.getSide(), order.getExchangePair(), order.getPrice());
                    return pool;
                })
                .addOrder(order);
    }

    public void removeOrder(Order order) {
        getPoolsMapBySide(order.getSide())
                .computeIfPresent(order.getPrice(), (price, pool) -> {
                    pool.removeOrder(order);
                    return pool.getOrders().isEmpty() ? null : pool;
                });
    }

    public Optional<Pool> getPoolByOrder(Order order) {
        return Optional.ofNullable(getPoolsMapBySide(order.getSide())
                .get(order.getPrice()));
    }

    public ConcurrentSkipListMap<BigDecimal, Pool> getPoolsMapBySide(Side side) {
        return (side == Side.SELL) ? sellPoolMap : buyPoolMap;
    }

    public ConcurrentSkipListMap<BigDecimal, Pool> getPoolsOppositeSide(Side side) {
        return (side == Side.BUY) ? sellPoolMap : buyPoolMap;
    }

    public BigDecimal getBestBuyPrice() {
        return sellPoolMap.isEmpty() ? null : sellPoolMap.firstKey();
    }

    public BigDecimal getBestSellPrice() {
        return buyPoolMap.isEmpty() ? null : buyPoolMap.firstKey();
    }

    public Long getOrderCount() {
        return orderCounter.get();
    }
}
