package org.valr.simulator;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;

import java.util.Random;

public class SimulatedTrader {
    private final Vertx vertx;
    private final SimulatedUser user;
    private final WebClient client;
    private final Random random = new Random();

    public SimulatedTrader(Vertx vertx, SimulatedUser user) {
        this.vertx = vertx;
        this.user = user;
        this.client = WebClient.create(vertx);
    }

    public void startTrading() {
        user.register(() -> user.login(() -> vertx.setPeriodic(1000, id -> placeRandomOrder())));
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
                    if (ar.succeeded()) {
//                        System.out.println(ar.result().bodyAsString());
//                        System.out.println("");
                        int i = 0;
                    } else {
                        System.err.println("❌ Order placement failed: " + ar.cause().getMessage());
                    }
                });

        client.post(8080, "localhost", "/api/orders/limit")
                .putHeader("Authorization", "Bearer " + user.getToken())
                .sendJson(order.buyOrder(), ar -> {
                    if (ar.succeeded()) {
//                        System.out.println(ar.result().bodyAsString());
//                        System.out.println("");
                        int i = 0;
                    } else {
                        System.err.println("❌ Order placement failed: " + ar.cause().getMessage());
                    }
                });
    }
}
