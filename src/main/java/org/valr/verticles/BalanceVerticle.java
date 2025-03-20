package org.valr.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.valr.middleware.AuthMiddleware;
import org.valr.model.Deposit;
import org.valr.registry.ServiceRegistry;
import org.valr.service.BalanceService;

public class BalanceVerticle extends AbstractVerticle {
    private final BalanceService balanceService;

    public BalanceVerticle(Router router, AuthMiddleware authMiddleware) {
        balanceService = ServiceRegistry.getBalanceService();
        setupRoutes(router, authMiddleware);
    }

    private void setupRoutes(Router router, AuthMiddleware authMiddleware) {
        router.post("/api/balance/deposit").handler(authMiddleware::authenticate).handler(this::depositCurrencyIntoAccount);
    }

    private void depositCurrencyIntoAccount(RoutingContext context) {
        context.request().bodyHandler(body -> {
            String userId = AuthMiddleware.getUserIdFromContext(context);
            Deposit deposit = Json.decodeValue(body, Deposit.class);
            balanceService.add(userId, deposit.getCurrency(), deposit.getAmount());
            context.response().setStatusCode(200).end();
        });
    }
}