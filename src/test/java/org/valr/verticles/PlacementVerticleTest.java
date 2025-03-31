package org.valr.verticles;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.valr.model.Order;
import org.valr.service.PlacementService;

import static org.mockito.Mockito.*;

// TODO: Figure out how to write a test for this async event bus
@ExtendWith(VertxExtension.class)
public class PlacementVerticleTest {

    private PlacementService placementService;

    @BeforeEach
    void setUp() {
        Vertx vertx = Vertx.vertx();
        placementService = mock(PlacementService.class);
        PlacementVerticle placementVerticle = new PlacementVerticle(null, null, null, placementService);
    }

    @Test
    void testStart(Vertx vertx, VertxTestContext testContext) {
        doNothing().when(placementService).placeLimitOrder(any(Order.class));
        testContext.completeNow();
    }
}
