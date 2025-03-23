package org.valr.auth;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthProviderTest {

    private JwtAuthProvider jwtAuthProvider;
    private JWTAuth mockJwtAuth;
    private Vertx vertx;

    @BeforeEach
    void setUp() {
        vertx = Vertx.vertx();
        mockJwtAuth = mock(JWTAuth.class);

        // Mocking the JwtAuth.create method
        try (var mockedStatic = mockStatic(JWTAuth.class)) {
            mockedStatic.when(() -> JWTAuth.create(any(Vertx.class), any(JWTAuthOptions.class)))
                    .thenReturn(mockJwtAuth);
            jwtAuthProvider = new JwtAuthProvider(vertx);
        }
    }

    @Test
    void testJwtAuthProviderInitialization() {
        assertNotNull(jwtAuthProvider.get());
    }

    @Test
    void testGenerateToken() {
        String userId = "testUser";
        String mockToken = "mockJwtToken";

        when(mockJwtAuth.generateToken(any(JsonObject.class), any(JWTOptions.class)))
                .thenReturn(mockToken);

        String token = jwtAuthProvider.generateToken(userId);

        assertEquals(mockToken, token);

        ArgumentCaptor<JsonObject> claimsCaptor = ArgumentCaptor.forClass(JsonObject.class);
        ArgumentCaptor<JWTOptions> optionsCaptor = ArgumentCaptor.forClass(JWTOptions.class);

        verify(mockJwtAuth).generateToken(claimsCaptor.capture(), optionsCaptor.capture());

        JsonObject capturedClaims = claimsCaptor.getValue();
        JWTOptions capturedOptions = optionsCaptor.getValue();

        assertEquals(userId, capturedClaims.getString("sub"));
        assertEquals(60 * 60 * 24 * 30, capturedOptions.getExpiresInSeconds());
    }

    @Test
    void testJwtAuthConfiguration() {
        ArgumentCaptor<JWTAuthOptions> optionsCaptor = ArgumentCaptor.forClass(JWTAuthOptions.class);

        try (var mockedStatic = Mockito.mockStatic(JWTAuth.class)) {
            mockedStatic.when(() -> JWTAuth.create(any(Vertx.class), optionsCaptor.capture()))
                    .thenReturn(mockJwtAuth);

            new JwtAuthProvider(vertx);

            JWTAuthOptions capturedOptions = optionsCaptor.getValue();
            assertNotNull(capturedOptions.getPubSecKeys());
            PubSecKeyOptions keyOptions = capturedOptions.getPubSecKeys().get(0);
            assertEquals("HS256", keyOptions.getAlgorithm());
        }
    }
}