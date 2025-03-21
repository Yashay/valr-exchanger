package org.valr.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.valr.middleware.AuthMiddleware;
import org.valr.middleware.ValidationMiddleware;
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
        router.post("/api/balance/deposit")
                .handler(authMiddleware::authenticate)
                .handler(new ValidationMiddleware<>(Deposit.class)::validate)
                .handler(this::depositCurrencyIntoAccount);
    }

    private void depositCurrencyIntoAccount(RoutingContext context) {
            String userId = AuthMiddleware.getUserIdFromContext(context);
            JsonObject data = context.body().asJsonObject();
            Deposit deposit = data.mapTo(Deposit.class);
            balanceService.add(userId, deposit.getCurrency(), deposit.getAmount());
            context.response().setStatusCode(200).end();
    }
}