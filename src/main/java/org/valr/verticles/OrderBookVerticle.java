package org.valr.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.valr.middleware.AuthMiddleware;
import org.valr.model.Order;
import org.valr.registry.ServiceRegistry;
import org.valr.service.OrderBookService;

public class OrderBookVerticle extends AbstractVerticle {
    private final OrderBookService orderBookService;

    public OrderBookVerticle(Router router, AuthMiddleware authMiddleware) {
        orderBookService = ServiceRegistry.getOrderBookService();
        setupRoutes(router, authMiddleware);
    }

    private void setupRoutes(Router router, AuthMiddleware authMiddleware) {
        router.post("/api/orders/limit").handler(authMiddleware::authenticate).handler(this::placeLimitOrder);
        router.get("/api/orderbook").handler(this::getOrderBookSnapshot);
    }

    private void getOrderBookSnapshot(RoutingContext context) {
        context.request().bodyHandler(body -> {
            JsonObject orderBookSnapshot = orderBookService.getOrderBookSnapshot("BTCZAR");
            context.response().setStatusCode(201).end(orderBookSnapshot.encodePrettily());
        });
    }

    private void placeLimitOrder(RoutingContext context) {
        context.request().bodyHandler(body -> {
            String userId = AuthMiddleware.getUserIdFromContext(context);
            Order order = Json.decodeValue(body, Order.class);
            JsonObject orderInformation = orderBookService.placeLimitOrder(userId, order);
            if(orderInformation.isEmpty()) {
                context.response().setStatusCode(201).end("Created");
            } else {
                context.response().setStatusCode(400).end(orderInformation.encodePrettily());
            }
        });
    }
}