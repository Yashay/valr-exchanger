package org.valr.middleware;

import com.google.inject.Inject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.web.RoutingContext;
import org.valr.auth.JwtAuthProvider;

public class AuthMiddleware {
    private final JwtAuthProvider jwtAuthProvider;

    @Inject
    public AuthMiddleware(JwtAuthProvider jwtAuthProvider) {
        this.jwtAuthProvider = jwtAuthProvider;
    }

    public void authenticate(RoutingContext context) {
        String authHeader = context.request().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            context.response().setStatusCode(401).end("Unauthorized");
            return;
        }
        String token = authHeader.substring(7);
        jwtAuthProvider.get().authenticate(new TokenCredentials(token))
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