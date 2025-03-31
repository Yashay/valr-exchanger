package org.valr.verticles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.vertx.core.AbstractVerticle;
import org.valr.model.Order;
import org.valr.service.PlacementService;

import java.io.IOException;

public class PlacementVerticle extends AbstractVerticle {
    private final PlacementService placementService;
    private final ObjectMapper objectMapper;

    @Inject
    public PlacementVerticle(ObjectMapper objectMapper, PlacementService placementService) {
        this.objectMapper = objectMapper;
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