package org.valr.simulator;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.valr.model.enums.Currency;

import java.math.BigDecimal;
import java.util.Random;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SimulatedDepositer {
    private Currency currency;
    private BigDecimal amount;
    private SimulatedUser user;
    private WebClient client;

    public SimulatedDepositer(WebClient client, Random random, SimulatedUser user) {
        this.user = user;
        currency = random.nextBoolean() ? Currency.BTC: Currency.ZAR;
        amount = BigDecimal.valueOf(random.nextInt(1000000, 1000000));
        this.client = client;
    }

    public JsonObject getDepositPayload() {
        return new JsonObject()
                .put("currency", currency)
                .put("amount", amount);
    }

    public void deposit(Runnable onSuccess) {
        client.post(8080, "localhost", "/api/balance/deposit")
                .putHeader("Authorization", "Bearer " + user.getToken())
                .sendJson(getDepositPayload(), ar -> {
                    if (ar.succeeded()) {
                        System.out.println("Deposit successful");
                        onSuccess.run();
                    } else {
                        System.err.println("Deposit failed: " + ar.cause().getMessage());
                    }
                });
    }
}
