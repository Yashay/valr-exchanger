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
import org.valr.repository.BalanceRepository;
import org.valr.service.BalanceService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(VertxExtension.class)
public class BalanceVerticleTest {

    private static final int TEST_PORT = 1212;

    private BalanceService balanceService;
    private BalanceRepository balanceRepository;
    private AuthMiddleware authMiddleware;
    private Router router;
    private WebClient webClient;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        balanceRepository = mock(BalanceRepository.class);
        balanceService = mock(BalanceService.class);
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

        BalanceVerticle verticle = new BalanceVerticle(router, authMiddleware, balanceService);
        vertx.deployVerticle(verticle, testContext.succeeding());
    }

    @Test
    void testDepositCurrencyIntoAccount(Vertx vertx, VertxTestContext testContext) {
        JsonObject depositJson = new JsonObject()
                .put("currency", "BTC") 
                .put("amount", 100);

        doNothing().when(balanceService).add(any(), any(), any());

        webClient.post("/api/balance/deposit")
                .sendJsonObject(depositJson)
                .onSuccess(response -> testContext.verify(() -> {
                    assertEquals(200, response.statusCode(), "Expected status 200");

                    verify(balanceService).add(any(), any(), any());

                    testContext.completeNow();
                }))
                .onFailure(testContext::failNow);
    }
}
