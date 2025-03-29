package org.valr.verticles;

import com.google.inject.Inject;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.valr.service.OrderBookService;

public class OrderBookVerticle extends AbstractVerticle {
    private final OrderBookService orderBookService;

    @Inject
    public OrderBookVerticle(Router router, OrderBookService orderBookService) {
        this.orderBookService = orderBookService;
        setupRoutes(router);
    }

    private void setupRoutes(Router router) {
        router.get("/api/orderbook")
                .handler(this::getOrderBookSnapshot);
    }

    private void getOrderBookSnapshot(RoutingContext context) {
        //TODO this should not be statically coded
        JsonObject orderBookSnapshot = orderBookService.getOrderBookSnapshot("BTCZAR");
        context.response().setStatusCode(201).end(orderBookSnapshot.encodePrettily());
    }
}