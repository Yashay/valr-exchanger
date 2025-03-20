package org.valr.model;

import lombok.*;

@Data
@AllArgsConstructor
@ToString
public class User {
    private final String userId;
    private final String username;
    private final String password;
}