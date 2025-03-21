package org.valr.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@ToString
public class User {
    private final String userId;

    @NotNull(message = "Username is required")
    @NotBlank(message = "Username cannot be blank")
    private final String username;

    @NotNull(message = "Password is required")
    @NotBlank(message = "Password cannot be blank")
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