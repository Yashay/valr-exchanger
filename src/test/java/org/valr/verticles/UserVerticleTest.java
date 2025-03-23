package org.valr.verticles;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.valr.auth.JwtAuthProvider;
import org.valr.middleware.AuthMiddleware;
import org.valr.model.User;
import org.valr.service.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(VertxExtension.class)
public class UserVerticleTest {

    private static final int TEST_PORT = 1212;

    private UserService userService;
    private JwtAuthProvider jwtAuthProvider;
    private AuthMiddleware authMiddleware;
    private Router router;
    private WebClient webClient;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        userService = mock(UserService.class);
        jwtAuthProvider = mock(JwtAuthProvider.class);
        authMiddleware = mock(AuthMiddleware.class);

        router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(TEST_PORT, testContext.succeedingThenComplete());

        vertx.deployVerticle(new UserVerticle(router, jwtAuthProvider, userService))
                .onComplete(testContext.succeeding());

        webClient = WebClient.create(vertx, new WebClientOptions()
                .setDefaultHost("localhost")
                .setDefaultPort(TEST_PORT));
    }

    @Test
    void testUserRegistration(Vertx vertx, VertxTestContext testContext) {
        JsonObject data = new JsonObject().put("username", "test1").put("password", "password1");

        when(userService.registerUser(any(), any())).thenReturn(true);

        webClient.post("/api/users/register")
                .sendJsonObject(data)
                .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
                    assertEquals(201, response.statusCode());
                    assertEquals("User registered successfully", response.bodyAsString());
                    testContext.completeNow();
                })));
    }

    @Test
    void testUserRegistrationUsernameTaken(Vertx vertx, VertxTestContext testContext) {
        JsonObject data = new JsonObject().put("username", "existingUser").put("password", "password1");

        when(userService.registerUser(any(), any())).thenReturn(false);

        webClient.post("/api/users/register")
                .sendJsonObject(data)
                .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
                    assertEquals(400, response.statusCode());
                    assertEquals("Username already taken", response.bodyAsString());
                    testContext.completeNow();
                })));
    }

    @Test
    void testUserLoginSuccess(Vertx vertx, VertxTestContext testContext) {
        JsonObject data = new JsonObject().put("username", "validUser").put("password", "password1");

        User mockUser = new User("validUser", "password1");
        when(jwtAuthProvider.generateToken(any())).thenReturn("mockToken");
        when(userService.authenticate(any(), any())).thenReturn(Optional.of(mockUser));

        webClient.post("/api/users/login")
                .sendJsonObject(data)
                .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
                    assertEquals(200, response.statusCode());
                    assertEquals("\"mockToken\"", response.bodyAsString());
                    testContext.completeNow();
                })));
    }

    @Test
    void testUserLoginInvalidCredentials(Vertx vertx, VertxTestContext testContext) {
        JsonObject data = new JsonObject().put("username", "invalidUser").put("password", "wrongPassword");

        when(userService.authenticate(any(), any())).thenReturn(Optional.empty());

        webClient.post("/api/users/login")
                .sendJsonObject(data)
                .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
                    assertEquals(401, response.statusCode());
                    assertEquals("Invalid credentials", response.bodyAsString());
                    testContext.completeNow();
                })));
    }

    @Test
    void testUserLoginWithTokenVerification(Vertx vertx, VertxTestContext testContext) {
        JsonObject data = new JsonObject().put("username", "validUser").put("password", "password1");

        User mockUser = new User("validUser", "password1");
        when(jwtAuthProvider.generateToken(any())).thenReturn("mockToken");
        when(userService.authenticate(any(), any())).thenReturn(Optional.of(mockUser));

        webClient.post("/api/users/login")
                .sendJsonObject(data)
                .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
                    assertEquals(200, response.statusCode());
                    verify(jwtAuthProvider).generateToken(any());
                    assertEquals("\"mockToken\"", response.bodyAsString());
                    testContext.completeNow();
                })));
    }
}
