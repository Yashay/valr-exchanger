package org.valr.verticles;

import com.google.inject.Inject;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.valr.middleware.AuthMiddleware;
import org.valr.service.TradeService;

public class TradeVerticle extends AbstractVerticle {
    private final TradeService tradeService;
    private final AuthMiddleware authMiddleware;

    @Inject
    public TradeVerticle(Router router, AuthMiddleware authMiddleware, TradeService tradeService) {
        this.tradeService = tradeService;
        this.authMiddleware = authMiddleware;
        setupRoutes(router);
    }

    private void setupRoutes(Router router) {
        router.get("/api/trade/history").handler(authMiddleware::authenticate).handler(this::getTradeHistorySnapshot);
    }

    private void getTradeHistorySnapshot(RoutingContext context) {
        String userId = AuthMiddleware.getUserIdFromContext(context);
        JsonArray userTradeHistory = tradeService.getRecentTradeHistory(userId);
        context.response().setStatusCode(200).end(userTradeHistory.encodePrettily());
    }
}