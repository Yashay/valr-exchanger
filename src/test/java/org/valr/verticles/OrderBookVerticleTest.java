package org.valr.verticles;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.valr.middleware.AuthMiddleware;
import org.valr.service.OrderBookService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
        authMiddleware = mock(AuthMiddleware.class);

        doAnswer(invocation -> {
            var context = (RoutingContext) invocation.getArgument(0);
            context.next();
            return null;
        }).when(authMiddleware).authenticate(Mockito.any());

        router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(TEST_PORT, testContext.succeedingThenComplete());

        webClient = WebClient.create(vertx, new WebClientOptions()
                .setDefaultHost("localhost")
                .setDefaultPort(TEST_PORT));

        OrderBookVerticle orderBookVerticle = new OrderBookVerticle(router, authMiddleware, orderBookService);
        vertx.deployVerticle(orderBookVerticle, testContext.succeeding());
    }

    @Test
    void testGetOrderBookSnapshotSuccess(Vertx vertx, VertxTestContext testContext) {
        JsonObject mockSnapshot = new JsonObject().put("bids", "[]").put("asks", "[]");
        when(orderBookService.getOrderBookSnapshot("BTCZAR")).thenReturn(mockSnapshot);

        webClient.get( "/api/orderbook")
                .send()
                .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
                    assertEquals(201, response.statusCode());
                    assertEquals(mockSnapshot.encodePrettily(), response.bodyAsString());
                    testContext.completeNow();
                })));
    }

    @Test
    void testPlaceLimitOrderSuccess(Vertx vertx, VertxTestContext testContext) {
        JsonObject orderData = new JsonObject()
                .put("side", "BUY")
                .put("exchangePair", "BTCZAR")
                .put("timeInForce", "GTC")
                .put("price", "50000")
                .put("quantity", "1");

        when(authMiddleware.getUserIdFromContext(any())).thenReturn("user123");
        when(orderBookService.placeLimitOrder(any(), any())).thenReturn(new JsonObject());

        webClient.post( "/api/orders/limit")
                .sendJsonObject(orderData)
                .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
                    assertEquals(201, response.statusCode());
                    assertEquals("Created", response.bodyAsString());
                    testContext.completeNow();
                })));
    }

    @Test
    void testPlaceLimitOrderValidationFails(Vertx vertx, VertxTestContext testContext) {
        JsonObject invalidOrder = new JsonObject().put("side", "BUY");

        webClient.post( "/api/orders/limit")
                .sendJsonObject(invalidOrder)
                .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
                    assertEquals(400, response.statusCode());
                    testContext.completeNow();
                })));
    }

    @Test
    void testPlaceLimitOrderFails(Vertx vertx, VertxTestContext testContext) {
        JsonObject orderData = new JsonObject()
                .put("side", "BUY")
                .put("exchangePair", "BTCZAR")
                .put("timeInForce", "GTC")
                .put("price", "50000")
                .put("quantity", "1");

        JsonObject errorResponse = new JsonObject();
        errorResponse.put("orderId", "1");
        errorResponse.put("placed", "false");
        errorResponse.put("message", "Insufficient funds");

        when(authMiddleware.getUserIdFromContext(any())).thenReturn("user123");
        when(orderBookService.placeLimitOrder(any(), any())).thenReturn(errorResponse);

        webClient.post( "/api/orders/limit")
                .sendJsonObject(orderData)
                .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
                    assertEquals(400, response.statusCode());
                    assertEquals(errorResponse.encodePrettily(), response.bodyAsString());
                    testContext.completeNow();
                })));
    }
}
