package org.valr.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import org.valr.middleware.AuthMiddleware;

public class MainVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startPromise) {
        Router router = Router.router(vertx);
        AuthMiddleware authMiddleware = new AuthMiddleware(vertx);

        vertx.deployVerticle(new UserVerticle(router));
        vertx.deployVerticle(new OrderBookVerticle(router, authMiddleware));
        vertx.deployVerticle(new TradeVerticle(router, authMiddleware));
        vertx.deployVerticle(new BalanceVerticle(router, authMiddleware));
//        vertx.deployVerticle(new SimulationManager());

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080, http -> {
                    if (http.succeeded()) {
                        System.out.println("Server started on port 8080");
                        startPromise.complete();
                    } else {
                        startPromise.fail(http.cause());
                    }
                });
    }
}