package org.valr.verticles;

import com.google.inject.Injector;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.valr.registry.AppRegistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
public class MainVerticleTest {

    private static final int TEST_PORT = 1212;

    private WebClient client;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        AppRegistry.injector = mock(Injector.class);

        Router mockRouter = Router.router(vertx);
        mockRouter.route().handler(BodyHandler.create());
        when(AppRegistry.injector.getInstance(Router.class)).thenReturn(mockRouter);

        vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));

        client = WebClient.create(vertx, new WebClientOptions()
                .setDefaultHost("localhost")
                .setDefaultPort(TEST_PORT));
    }

    @Test
    void testMainVerticleDeployment(Vertx vertx, VertxTestContext testContext) {
        vertx.createHttpServer()
                .requestHandler(request -> {
                    request.response().setStatusCode(200).end();
                })
                .listen(TEST_PORT, http -> {
                    if (http.succeeded()) {
                        client.get("/api/orderbook").send()
                                .onSuccess(response -> testContext.verify(() -> {
                                    assertEquals(200, response.statusCode(), "Expected status 200");
                                    testContext.completeNow();
                                }))
                                .onFailure(testContext::failNow);
                    } else {
                        testContext.failNow(http.cause());
                    }
                });
    }
}
