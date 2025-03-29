package org.valr.verticles;

import com.google.inject.Inject;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.valr.middleware.AuthMiddleware;
import org.valr.middleware.ValidationMiddleware;
import org.valr.model.Deposit;
import org.valr.service.BalanceService;

public class BalanceVerticle extends AbstractVerticle {
    private final BalanceService balanceService;
    private final AuthMiddleware authMiddleware;

    @Inject
    public BalanceVerticle(Router router, AuthMiddleware authMiddleware, BalanceService balanceService) {
        this.balanceService = balanceService;
        this.authMiddleware = authMiddleware;
        setupRoutes(router);
    }

    private void setupRoutes(Router router) {
        router.post("/api/balance/deposit")
                .handler(authMiddleware::authenticate)
                .handler(new ValidationMiddleware<>(Deposit.class)::validate)
                .handler(this::depositCurrencyIntoAccount);
    }

    private void depositCurrencyIntoAccount(RoutingContext context) {
        String userId = authMiddleware.getUserIdFromContext(context);
        JsonObject data = context.body().asJsonObject();
        Deposit deposit = data.mapTo(Deposit.class);
        balanceService.add(userId, deposit.getCurrency(), deposit.getAmount());
        context.response().setStatusCode(200).end();
    }
}