package org.valr.verticles;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.valr.middleware.AuthMiddleware;
import org.valr.service.OrderBookService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(VertxExtension.class)
public class OrderBookVerticleTest {

    private static final int TEST_PORT = 1212;

    private OrderBookService orderBookService;
    private AuthMiddleware authMiddleware;
    private Router router;
    private WebClient webClient;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        orderBookService = mock(OrderBookService.class);

        router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(TEST_PORT, testContext.succeedingThenComplete());

        webClient = WebClient.create(vertx, new WebClientOptions()
                .setDefaultHost("localhost")
                .setDefaultPort(TEST_PORT));

        OrderBookVerticle orderBookVerticle = new OrderBookVerticle(router, orderBookService);
        vertx.deployVerticle(orderBookVerticle, testContext.succeeding());
    }

    @Test
    void testGetOrderBookSnapshotSuccess(Vertx vertx, VertxTestContext testContext) {
        JsonObject mockSnapshot = new JsonObject().put("bids", "[]").put("asks", "[]");
        when(orderBookService.getOrderBookSnapshot()).thenReturn(mockSnapshot);

        webClient.get( "/api/orderbook")
                .send()
                .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
                    assertEquals(201, response.statusCode());
                    assertEquals(mockSnapshot.encodePrettily(), response.bodyAsString());
                    testContext.completeNow();
                })));
    }
}
