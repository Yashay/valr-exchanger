package org.valr.verticles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import org.valr.middleware.AuthMiddleware;
import org.valr.model.Order;
import org.valr.registry.AppRegistry;
import org.valr.service.PlacementService;

import java.io.IOException;

public class PlacementVerticle extends AbstractVerticle {
    private final PlacementService placementService;
    private final AuthMiddleware authMiddleware;
    private final ObjectMapper objectMapper;

    @Inject
    public PlacementVerticle(Router router, ObjectMapper objectMapper, AuthMiddleware authMiddleware, PlacementService placementService) {
        this.objectMapper = objectMapper;
        this.authMiddleware = authMiddleware;
        this.placementService = placementService;
    }

    @Override
    public void start() {
        vertx.eventBus().consumer("order.queue", o -> {
            try {
                Order order = objectMapper.readValue((byte[]) o.body(), Order.class);
                placementService.placeLimitOrder(order);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}