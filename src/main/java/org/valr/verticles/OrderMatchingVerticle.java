package org.valr.verticles;

import io.vertx.core.AbstractVerticle;
import org.valr.registry.ServiceRegistry;
import org.valr.service.MatchingService;
import org.valr.service.OrderBookService;

public class OrderMatchingVerticle extends AbstractVerticle {
    private final MatchingService matchingService;
    private final OrderBookService orderBookService;

    public OrderMatchingVerticle() {
        this.matchingService = ServiceRegistry.getMatchingService();
        this.orderBookService = ServiceRegistry.getOrderBookService();
    }

    @Override
    public void start() {
        vertx.setPeriodic(5000, id -> {
            int i = 0;
        });
    }
}