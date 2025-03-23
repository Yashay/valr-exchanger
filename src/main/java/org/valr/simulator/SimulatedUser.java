package org.valr.simulator;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import lombok.Getter;
import lombok.Setter;

import java.util.Random;

@Getter
@Setter
public class SimulatedUser {
    private final String username;
    private final String password;
    private String token;
    private final WebClient client;


    public SimulatedUser(WebClient client) {
        Random random = new Random();
        username = "User" + random.nextInt(1000000);
        password = "Pass" + random.nextInt(1000000);
        this.client = client;
    }

    public JsonObject getRegistrationPayload() {
        return new JsonObject()
                .put("username", username)
                .put("password", password);
    }

    public JsonObject getLoginPayload() {
        return new JsonObject()
                .put("username", username)
                .put("password", password);
    }

    public void register(Runnable onSuccess) {
        client.post(8080, "localhost", "/api/users/register")
                .sendJson(getRegistrationPayload(), ar -> {
                    if (ar.succeeded()) {
                        onSuccess.run();
                    } else {
                        System.err.println("Registration failed: " + ar.cause().getMessage());
                    }
                });
    }

    public void login(Runnable onSuccess) {
        client.post(8080, "localhost", "/api/users/login")
                .sendJson(getLoginPayload(), ar -> {
                    if (ar.succeeded()) {
                        HttpResponse<?> response = ar.result();
                        this.token = response.bodyAsString().replace("\"", "");
                        onSuccess.run();
                    } else {
                        System.err.println("Login failed for " + username);
                    }
                });
    }
}
