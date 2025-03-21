package org.valr.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@ToString
public class User {
    private final String userId;
    private final String username;
    private final String password;

    //TODO consider moving this to DTO
    @JsonCreator
    public User(
            @JsonProperty("username") String username,
            @JsonProperty("password") String password) {
        this.userId = UUID.randomUUID().toString();
        this.username = username;
        this.password = password;
    }
}