package org.valr.verticles;

import com.google.inject.Inject;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.valr.auth.JwtAuthProvider;
import org.valr.middleware.AuthMiddleware;
import org.valr.middleware.ValidationMiddleware;
import org.valr.model.User;
import org.valr.service.UserService;

import java.util.Optional;

public class UserVerticle extends AbstractVerticle {
    private final UserService userService;
    private final JwtAuthProvider jwtAuthProvider = new JwtAuthProvider(vertx);

    @Inject
    public UserVerticle(Router router, AuthMiddleware authMiddleware, UserService userService) {
        this.userService = userService;
        setupRoutes(router);
    }

    private void setupRoutes(Router router) {
        ValidationMiddleware<User> validationMiddleware = new ValidationMiddleware<>(User.class);
        router.post("/api/users/register").handler(validationMiddleware::validate).handler(this::registerUser);
        router.post("/api/users/login").handler(validationMiddleware::validate).handler(this::loginUser);
    }

    private void registerUser(RoutingContext context) {
        JsonObject data = context.body().asJsonObject();
        User user = data.mapTo(User.class);
        boolean registered = userService.registerUser(user.getUsername(), user.getPassword());

        if (registered) {
            context.response().setStatusCode(201).end("User registered successfully");
        } else {
            context.response().setStatusCode(400).end("Username already taken");
        }
    }

    private void loginUser(RoutingContext context) {
        JsonObject data = context.body().asJsonObject();
        User user = data.mapTo(User.class);
        Optional<User> authenticatedUser = userService.authenticate(user.getUsername(), user.getPassword());

        if (authenticatedUser.isPresent()) {
            String token = jwtAuthProvider.generateToken(authenticatedUser.get().getUserId());
            context.response().putHeader("Content-Type", "application/json").end(Json.encodePrettily(token));
        } else {
            context.response().setStatusCode(401).end("Invalid credentials");
        }
    }
}