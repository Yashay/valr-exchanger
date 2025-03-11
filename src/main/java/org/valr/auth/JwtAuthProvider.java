package org.valr.auth;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.auth.PubSecKeyOptions;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Base64;

public class JwtAuthProvider {
    private static final String JWT_HASHING_ALGORITHM = "HS256";
    private static final String KEY_HASHING_ALGORITHM = "HmacSHA256";
    private static final int EXPIRATION_TIME = 60 * 60 * 24;
    // hard to test
    private static final String SECRET_KEY = generateSecretKey();
    private final JWTAuth jwtAuth;

    public JwtAuthProvider(Vertx vertx) {
        jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions()
                        .setAlgorithm(JWT_HASHING_ALGORITHM)
                        .setBuffer(SECRET_KEY)));
    }

    public JWTAuth getAuthProvider() {
        return jwtAuth;
    }

    public String generateToken(String userId) {
        JsonObject claims = new JsonObject();
        claims.put("sub", userId);
        return jwtAuth.generateToken(claims, new JWTOptions()
                .setExpiresInSeconds(EXPIRATION_TIME));
    }

    private static String generateSecretKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(KEY_HASHING_ALGORITHM);
            SecretKey key = keyGen.generateKey();
            String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
            return base64Key;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JWT secret key", e);
        }
    }
}
