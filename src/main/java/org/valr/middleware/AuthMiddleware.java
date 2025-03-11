package org.valr.middleware;

import io.vertx.core.Vertx;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;
import org.valr.auth.JwtAuthProvider;

public class AuthMiddleware {
    private final JWTAuth jwtAuth;

    public AuthMiddleware(Vertx vertx) {
        this.jwtAuth = new JwtAuthProvider(vertx).getAuthProvider();
    }

    public void authenticate(RoutingContext context) {
        String authHeader = context.request().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            context.response().setStatusCode(401).end("Unauthorized");
            return;
        }
        String token = authHeader.substring(7);
        jwtAuth.authenticate(new TokenCredentials(token))
                .onSuccess(user -> {
                    context.put("user", user);
                    context.next();
                })
                .onFailure(err -> {
                    context.response()
                            .setStatusCode(401)
                            .end("Invalid token");
                });
    }

    public static String getUserIdFromContext(RoutingContext context) {
        User user = context.get("user");
        if (user != null) {
            return user.principal().getString("sub");
        }
        return null;
    }
}