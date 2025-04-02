package org.valr.verticles;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.valr.model.Order;
import org.valr.model.enums.Side;
import org.valr.service.PlacementService;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(VertxExtension.class)
public class PlacementVerticleTest {

    private PlacementService placementService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        placementService = mock(PlacementService.class);
        objectMapper = mock(ObjectMapper.class);
        PlacementVerticle placementVerticle = new PlacementVerticle(objectMapper, placementService);
        vertx.deployVerticle(placementVerticle, testContext.succeeding(id -> testContext.completeNow()));
    }

    @Test
    void testStart(Vertx vertx, VertxTestContext testContext) {
        doNothing().when(placementService).placeLimitOrder(any(Order.class));
        testContext.completeNow();
    }

    @Test
    void testOrderPlacement(Vertx vertx, VertxTestContext testContext) throws IOException {
        JsonObject orderJson = new JsonObject();
        orderJson.put("side", Side.BUY);
        orderJson.put("quantity", 100);
        orderJson.put("price", 100);

        Order order = orderJson.mapTo(Order.class);
        byte[] orderbin = orderJson.toBuffer().getBytes();

        doNothing().when(placementService).placeLimitOrder(any(Order.class));
        when(objectMapper.readValue(any(byte[].class), eq(Order.class))).thenReturn(order);

        vertx.eventBus().request("order.queue", orderbin, reply -> {
            verify(placementService).placeLimitOrder(any(Order.class));
            testContext.completeNow();
        });
    }

    @Test
    void testCorruptOrderPlacement(Vertx vertx, VertxTestContext testContext) throws IOException {
        byte[] corruptOrderbin = new byte[5];

        doNothing().when(placementService).placeLimitOrder(any(Order.class));
        when(objectMapper.readValue(any(byte[].class), eq(Order.class))).thenThrow(new IOException());

        vertx.eventBus().request("order.queue", corruptOrderbin, reply -> {
            verify(placementService, never()).placeLimitOrder(any(Order.class));
            testContext.completeNow();
        });
    }

}
