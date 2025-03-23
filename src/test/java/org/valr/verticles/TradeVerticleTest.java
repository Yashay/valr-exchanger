package org.valr.verticles;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
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
import org.mockito.Mockito;
import org.valr.middleware.AuthMiddleware;
import org.valr.service.TradeService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(VertxExtension.class)
public class TradeVerticleTest {

    private static final int TEST_PORT = 1212;

    private TradeService tradeService;
    private AuthMiddleware authMiddleware;
    private Router router;
    private WebClient client;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        tradeService = mock(TradeService.class);
        authMiddleware = mock(AuthMiddleware.class);

        doAnswer(invocation -> {
            var context = (io.vertx.ext.web.RoutingContext) invocation.getArgument(0);
            context.next(); 
            return null;
        }).when(authMiddleware).authenticate(Mockito.any());

        router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(TEST_PORT, testContext.succeedingThenComplete());

        client = WebClient.create(vertx, new WebClientOptions()
                .setDefaultHost("localhost")
                .setDefaultPort(TEST_PORT));

        TradeVerticle verticle = new TradeVerticle(router, authMiddleware, tradeService);
        vertx.deployVerticle(verticle, testContext.succeeding());
    }

    @Test
    void testGetTradeHistory(Vertx vertx, VertxTestContext testContext) {
        JsonArray mockTradeHistory = new JsonArray()
                .add(new JsonObject().put("tradeId", "123").put("price", 50000).put("quantity", 1))
                .add(new JsonObject().put("tradeId", "124").put("price", 51000).put("quantity", 2));

        when(tradeService.getRecentTradeHistory(any())).thenReturn(mockTradeHistory);

        client.get("/api/trade/history")
                .send()
                .onSuccess(response -> testContext.verify(() -> {
                    assertEquals(200, response.statusCode(), "Expected status 200");
                    assertEquals(mockTradeHistory.encodePrettily(), response.bodyAsString(), "Trade history matches");
                    testContext.completeNow();
                }))
                .onFailure(testContext::failNow);
    }
}
