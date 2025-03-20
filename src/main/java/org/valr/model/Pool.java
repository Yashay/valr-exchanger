package org.valr.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

@Setter
@Getter
@AllArgsConstructor
public class Pool implements Comparable<Pool> {
    private final BigDecimal price;
    private AtomicReference<BigDecimal> volume;
    private ConcurrentSkipListSet<Order> orders;

    public Pool(BigDecimal price) {
        this.price = price;
        volume = new AtomicReference<>(BigDecimal.ZERO);
        orders = new ConcurrentSkipListSet<>();
    }

    public void reduceQuantity(BigDecimal quantity) {
        volume.getAndUpdate(o -> o.subtract(quantity));
    }

    public void addOrder(Order order) {
        orders.add(order);
        volume.getAndUpdate(o -> o.add(order.getQuantity()));
    }

    public void removeOrder(Order order) {
        orders.remove(order);
        volume.getAndUpdate(o -> o.subtract(order.getQuantity()));
    }

    @Override
    public int compareTo(Pool o) {
        return price.compareTo(o.price);
    }
}
