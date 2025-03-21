package org.valr.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.valr.middleware.AuthMiddleware;
import org.valr.middleware.ValidationMiddleware;
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
        router.post("/api/orders/limit")
                .handler(authMiddleware::authenticate)
                //TODO better way to do this? [IDEA injection]
                .handler(new ValidationMiddleware<>(Order.class)::validate)
                .handler(this::placeLimitOrder);
        router.get("/api/orderbook")
                .handler(this::getOrderBookSnapshot);
    }

    private void getOrderBookSnapshot(RoutingContext context) {
        //TODO this should not be statically coded
        JsonObject orderBookSnapshot = orderBookService.getOrderBookSnapshot("BTCZAR");
        context.response().setStatusCode(201).end(orderBookSnapshot.encodePrettily());
    }

    private void placeLimitOrder(RoutingContext context) {
        String userId = AuthMiddleware.getUserIdFromContext(context);
        JsonObject data = context.body().asJsonObject();
        Order order = data.mapTo(Order.class);
        JsonObject orderInformation = orderBookService.placeLimitOrder(userId, order);
        if (orderInformation.isEmpty()) {
            context.response().setStatusCode(201).end("Created");
        } else {
            context.response().setStatusCode(400).end(orderInformation.encodePrettily());
        }
    }
}