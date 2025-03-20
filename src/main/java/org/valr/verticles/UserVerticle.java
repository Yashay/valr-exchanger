package org.valr.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.valr.auth.JwtAuthProvider;
import org.valr.model.User;
import org.valr.registry.ServiceRegistry;
import org.valr.service.UserService;

import java.util.Optional;

public class UserVerticle extends AbstractVerticle {
    private final UserService userService;
    private final JwtAuthProvider jwtAuthProvider = new JwtAuthProvider(vertx);

    public UserVerticle(Router router) {
        userService = ServiceRegistry.getUserService();
        setupRoutes(router);
    }

    private void setupRoutes(Router router) {
        router.post("/api/users/register").consumes("application/json").handler(this::registerUser);
        router.post("/api/users/login").consumes("application/json").handler(this::loginUser);
    }

    private void registerUser(RoutingContext context) {
        context.request().bodyHandler(body -> {
            User user = Json.decodeValue(body, User.class);
            boolean registered = userService.registerUser(user.getUsername(), user.getPassword());

            if (registered) {
                context.response().setStatusCode(201).end("User registered successfully");
            } else {
                context.response().setStatusCode(400).end("Username already taken");
            }
        });
    }

    private void loginUser(RoutingContext context) {
        context.request().bodyHandler(body -> {
            User user = Json.decodeValue(body, User.class);
            Optional<User> authenticatedUser = userService.authenticate(user.getUsername(), user.getPassword());

            if (authenticatedUser.isPresent()) {
                String token = jwtAuthProvider.generateToken(authenticatedUser.get().getUserId());
                context.response().putHeader("Content-Type", "application/json").end(Json.encodePrettily(token));
            } else {
                context.response().setStatusCode(401).end("Invalid credentials");
            }
        });
    }
}