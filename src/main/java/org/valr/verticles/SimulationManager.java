package org.valr.verticles;

import io.vertx.core.AbstractVerticle;
import org.valr.simulator.SimulatedTrader;
import org.valr.simulator.SimulatedUser;

import java.util.ArrayList;
import java.util.List;

public class SimulationManager extends AbstractVerticle {
    private static final int USER_COUNT = 2;
    private final List<SimulatedTrader> traders = new ArrayList<>();

    @Override
    public void start() {
        for (int i = 0; i < USER_COUNT; i++) {
            SimulatedUser user = new SimulatedUser(vertx);
            SimulatedTrader trader = new SimulatedTrader(vertx, user);
            traders.add(trader);
        }

        vertx.setTimer(2000, id -> startTrading());
    }

    private void startTrading() {
        for (SimulatedTrader trader : traders) {
            trader.startTrading();
        }
    }
}
