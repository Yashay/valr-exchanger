package org.valr.verticles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.valr.model.Order;
import org.valr.service.BalanceService;
import org.valr.service.OrderBookService;
import org.valr.service.PlacementService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(VertxExtension.class)
public class OrderBookVerticleTest {

    private static final int TEST_PORT = 1212;

    private OrderBookService orderBookService;
    private BalanceService balanceService;
    private AuthMiddleware authMiddleware;
    private ObjectMapper objectMapper;
    private Router router;
    private WebClient webClient;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        orderBookService = mock(OrderBookService.class);
        balanceService = mock(BalanceService.class);
        authMiddleware = mock(AuthMiddleware.class);
        objectMapper = mock(ObjectMapper.class);

        router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        doAnswer(invocation -> {
            var context = (RoutingContext) invocation.getArgument(0);
            context.next();
            return null;
        }).when(authMiddleware).authenticate(Mockito.any());

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(TEST_PORT, testContext.succeedingThenComplete());

        webClient = WebClient.create(vertx, new WebClientOptions()
                .setDefaultHost("localhost")
                .setDefaultPort(TEST_PORT));

        OrderBookVerticle orderBookVerticle = new OrderBookVerticle(router, objectMapper, authMiddleware, orderBookService, balanceService);
        vertx.deployVerticle(orderBookVerticle, testContext.succeeding());
    }

    @Test
    void testGetOrderBookSnapshotSuccess(VertxTestContext testContext) {
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


    @Test
    void testPlaceLimitOrderInQueueSuccess(VertxTestContext testContext) {
        JsonObject orderData = new JsonObject()
                .put("side", "BUY")
                .put("exchangePair", "BTCZAR")
                .put("timeInForce", "GTC")
                .put("price", "50000")
                .put("quantity", "1");

        RoutingContext mockRoutingContext = mock(RoutingContext.class);
        when(mockRoutingContext.get(Order.class.getSimpleName())).thenReturn(orderData);
        when(authMiddleware.getUserIdFromContext(any())).thenReturn("user123");
        when(balanceService.reserveOnOrder(any())).thenReturn(true);

        webClient.post( "/api/orders/limit")
                .sendJsonObject(orderData)
                .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
                    assertEquals(201, response.statusCode());
                    assertEquals("Created", response.bodyAsString());
                    testContext.completeNow();
                }))).onFailure(testContext::failNow);
    }

    @Test
    void testPlaceLimitOrderInQueueValidationFails(VertxTestContext testContext) {
        JsonObject invalidOrder = new JsonObject().put("side", "BUY");

        webClient.post( "/api/orders/limit")
                .sendJsonObject(invalidOrder)
                .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
                    assertEquals(400, response.statusCode());
                    testContext.completeNow();
                })));
    }

    @Test
    void testPlaceLimitOrderQueueFails(VertxTestContext testContext) {
        JsonObject orderData = new JsonObject()
                .put("orderId", "1")
                .put("side", "BUY")
                .put("exchangePair", "BTCZAR")
                .put("timeInForce", "GTC")
                .put("price", "50000")
                .put("quantity", "1");

        JsonObject errorResponse = new JsonObject();
        errorResponse.put("orderId", "1");
        errorResponse.put("placed", false);
        errorResponse.put("message", "Insufficient funds");

        when(authMiddleware.getUserIdFromContext(any())).thenReturn("user123");
        when(balanceService.reserveOnOrder(any())).thenReturn(false);

        webClient.post( "/api/orders/limit")
                .sendJsonObject(orderData)
                .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
                    assertEquals(400, response.statusCode());
                    testContext.completeNow();
                })));
    }

    @Test
    void testPlaceLimitOrderQueueFailsOnException(VertxTestContext testContext) throws JsonProcessingException {
        JsonObject orderData = new JsonObject()
                .put("orderId", "1")
                .put("side", "BUY")
                .put("exchangePair", "BTCZAR")
                .put("timeInForce", "GTC")
                .put("price", "50000")
                .put("quantity", "1");

        when(authMiddleware.getUserIdFromContext(any())).thenReturn("user123");
        when(balanceService.reserveOnOrder(any())).thenReturn(true);
        when(objectMapper.writeValueAsBytes(any(Order.class))).thenThrow(new RuntimeException());

        webClient.post( "/api/orders/limit")
                .sendJsonObject(orderData)
                .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
                    assertEquals("Failure", response.bodyAsString());
                    assertEquals(400, response.statusCode());
                    testContext.completeNow();
                })));
    }
}
