package org.valr.middleware;

import io.vertx.core.Future;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.valr.auth.JwtAuthProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class AuthMiddlewareTest {
    private AuthMiddleware authMiddleware;
    private JWTAuth jwtAuthMock;
    private RoutingContext contextMock;
    private HttpServerRequest requestMock;
    private HttpServerResponse responseMock;
    private User mockUser;

    @BeforeEach
    void setUp() {
        jwtAuthMock = mock(JWTAuth.class);
        JwtAuthProvider jwtAuthProviderMock = mock(JwtAuthProvider.class);
        when(jwtAuthProviderMock.get()).thenReturn(jwtAuthMock);

        authMiddleware = new AuthMiddleware(jwtAuthProviderMock);
        contextMock = mock(RoutingContext.class);

        contextMock = mock(RoutingContext.class);
        requestMock = mock(HttpServerRequest.class);
        responseMock = mock(HttpServerResponse.class);
        mockUser = mock(User.class);

        when(contextMock.request()).thenReturn(requestMock);
        when(contextMock.response()).thenReturn(responseMock);

        when(responseMock.setStatusCode(anyInt())).thenReturn(responseMock);
        when(responseMock.end(anyString())).thenReturn(Future.succeededFuture());
    }

    @Test
    void testAuthenticateInvalidToken() {
        String token = "invalid.jwt.token";
        when(contextMock.request()).thenReturn(mock(io.vertx.core.http.HttpServerRequest.class));
        when(contextMock.request().getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtAuthMock.authenticate(any(TokenCredentials.class))).thenReturn(Future.failedFuture("Invalid token"));

        authMiddleware.authenticate(contextMock);

        verify(contextMock.response()).setStatusCode(401);
        verify(contextMock.response()).end("Invalid token");
    }

    @Test
    void testAuthenticateMissingAuthorizationHeader() {
        when(contextMock.request()).thenReturn(mock(io.vertx.core.http.HttpServerRequest.class));
        when(contextMock.request().getHeader("Authorization")).thenReturn(null);

        authMiddleware.authenticate(contextMock);

        verify(contextMock.response()).setStatusCode(401);
        verify(contextMock.response()).end("Unauthorized");
    }

    @Test
    void testGetUserIdFromContextUserPresent() {
        User mockUser = mock(User.class);
        when(mockUser.principal()).thenReturn(io.vertx.core.json.JsonObject.of("sub", "12345"));
        when(contextMock.get("user")).thenReturn(mockUser);

        String userId = authMiddleware.getUserIdFromContext(contextMock);

        assertEquals("12345", userId);
    }

    @Test
    void testGetUserIdFromContextNoUser() {
        when(contextMock.get("user")).thenReturn(null);

        String userId = authMiddleware.getUserIdFromContext(contextMock);

        assertNull(userId);
    }
}
