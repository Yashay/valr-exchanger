package org.valr.verticles;

import com.google.inject.Inject;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.valr.middleware.AuthMiddleware;
import org.valr.middleware.ValidationMiddleware;
import org.valr.model.Order;
import org.valr.service.PlacementService;

public class PlacementVerticle extends AbstractVerticle {
    private final PlacementService placementService;
    private final AuthMiddleware authMiddleware;

    @Inject
    public PlacementVerticle(Router router, AuthMiddleware authMiddleware, PlacementService placementService) {
        this.authMiddleware = authMiddleware;
        this.placementService = placementService;
        setupRoutes(router);
    }

    private void setupRoutes(Router router) {
        router.post("/api/orders/limit")
                .handler(authMiddleware::authenticate)
                .handler(new ValidationMiddleware<>(Order.class)::validate)
                .handler(this::placeLimitOrder);
    }

    private void placeLimitOrder(RoutingContext context) {
        String userId = authMiddleware.getUserIdFromContext(context);
        JsonObject data = context.body().asJsonObject();

        Order order = data.mapTo(Order.class);
        order.setUserId(userId);

        JsonObject orderInformation = placementService.placeLimitOrder(order);
        if (orderInformation.isEmpty()) {
            context.response().setStatusCode(201).end("Created");
        } else {
            context.response().setStatusCode(400).end(orderInformation.encodePrettily());
        }
    }
}