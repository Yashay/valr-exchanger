package org.valr.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.valr.middleware.AuthMiddleware;
import org.valr.registry.ServiceRegistry;
import org.valr.service.TradeService;

public class TradeVerticle extends AbstractVerticle {
    private final TradeService tradeService;

    public TradeVerticle(Router router, AuthMiddleware authMiddleware) {
        tradeService = ServiceRegistry.getTradeService();
        setupRoutes(router, authMiddleware);
    }

    private void setupRoutes(Router router, AuthMiddleware authMiddleware) {
        router.get("/api/trade/history").handler(authMiddleware::authenticate).handler(this::getTradeHistorySnapshot);
    }

    private void getTradeHistorySnapshot(RoutingContext context) {
        String userId = AuthMiddleware.getUserIdFromContext(context);
        JsonArray userTradeHistory = tradeService.getRecentTradeHistory(userId);
        context.response().setStatusCode(200).end(userTradeHistory.encodePrettily());
    }
}