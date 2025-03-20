package org.valr.model;

import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.valr.model.enums.ExchangePair;
import org.valr.model.enums.Side;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

@Setter
@Getter
@AllArgsConstructor
public class Pool implements Comparable<Pool> {
    private final BigDecimal price;
    private final Side side;
    private final ExchangePair currencyPair;
    private final AtomicReference<BigDecimal> volume;
    private final ConcurrentSkipListSet<Order> orders;

    public Pool(BigDecimal price) {
        this.price = price;
        this.side = null;
        this.currencyPair = null;
        this.volume = new AtomicReference<>(BigDecimal.ZERO);
        this.orders = new ConcurrentSkipListSet<>();
    }

    public Pool(Side side, ExchangePair currencyPair, BigDecimal price) {
        this.price = price;
        this.side = side;
        this.currencyPair = currencyPair;
        this.volume = new AtomicReference<>(BigDecimal.ZERO);
        this.orders = new ConcurrentSkipListSet<>();
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

    public JsonObject toJson() {
        JsonObject poolJson = new JsonObject();
        poolJson.put("side", getSide());
        poolJson.put("quantity", getVolume());
        poolJson.put("price", getPrice());
        poolJson.put("currencyPair", getCurrencyPair());
        poolJson.put("orderCount", getOrders().size());
        return poolJson;
    }

    @Override
    public int compareTo(Pool o) {
        return price.compareTo(o.price);
    }
}
