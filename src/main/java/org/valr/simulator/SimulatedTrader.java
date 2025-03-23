package org.valr.simulator;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;

import java.util.Random;

public class SimulatedTrader {
    private final Vertx vertx;
    private final SimulatedUser user;
    private final SimulatedDepositer depositer;
    private final WebClient client;
    private final Random random;

    public SimulatedTrader(Vertx vertx, SimulatedUser user, SimulatedDepositer depositer, WebClient client, Random random) {
        this.vertx = vertx;
        this.user = user;
        this.depositer = depositer;
        this.client = client;
        this.random = random;
    }

    public void startTrading() {
        user.register(() -> user.login(() -> vertx.setPeriodic(10, id -> placeRandomOrder())));
    }

    private void placeRandomOrder() {
        if (user.getToken() == null) {
            System.err.println("User " + user.getUsername() + " has no token. Skipping order.");
            return;
        }

        SimulatedOrder order = new SimulatedOrder(random);
        client.post(8080, "localhost", "/api/orders/limit")
                .putHeader("Authorization", "Bearer " + user.getToken())
                .sendJson(order.sellOrder(), ar -> {
                    if (!ar.succeeded()) {
                        System.err.println("Order placement failed: " + ar.cause().getMessage());
                    }
                });

        client.post(8080, "localhost", "/api/orders/limit")
                .putHeader("Authorization", "Bearer " + user.getToken())
                .sendJson(order.buyOrder(), ar -> {
                    if (!ar.succeeded()) {
                        System.err.println("Order placement failed: " + ar.cause().getMessage());
                    }
                });
    }
}
