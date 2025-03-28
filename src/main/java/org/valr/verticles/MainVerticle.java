package org.valr.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.valr.registry.AppRegistry;

public class MainVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startPromise) {
        Router router = AppRegistry.injector.getInstance(Router.class);
        router.route().handler(BodyHandler.create());

        vertx.deployVerticle(AppRegistry.injector.getInstance(UserVerticle.class));
        // force the placement algorithm to run on one thread to prevent data drift
        // TODO: Add verticle deployment configuration class
        vertx.deployVerticle(AppRegistry.injector.getInstance(PlacementVerticle.class), new DeploymentOptions().setInstances(1));
        vertx.deployVerticle(AppRegistry.injector.getInstance(OrderBookVerticle.class));
        vertx.deployVerticle(AppRegistry.injector.getInstance(TradeVerticle.class));
        vertx.deployVerticle(AppRegistry.injector.getInstance(BalanceVerticle.class));

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