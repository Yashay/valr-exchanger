package org.valr.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.client.WebClient;
import org.valr.simulator.SimulatedTrader;
import org.valr.simulator.SimulatedUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimulationManager extends AbstractVerticle {
    private static final int USER_COUNT = 10;
    private final List<SimulatedTrader> traders = new ArrayList<>();

    @Override
    public void start() {
        Random random = new Random();
            vertx.executeBlocking(promise -> {
                for (int i = 0; i < USER_COUNT; i++) {
                        WebClient client = WebClient.create(vertx);
                        SimulatedUser user = new SimulatedUser(client);
                        // SimulatedDepositer depositer = new SimulatedDepositer(client, random, user);
                        SimulatedTrader trader = new SimulatedTrader(vertx, user, null, client, random);
                        traders.add(trader);
                        promise.complete();
                }
            }, res -> {
                if (res.succeeded()) {
                    System.out.println("Execution complete!");
                }
            });

        vertx.setTimer(2000, id -> startTrading());
    }

    private void startTrading() {
        for (SimulatedTrader trader : traders) {
            trader.startTrading();
        }
    }
}
