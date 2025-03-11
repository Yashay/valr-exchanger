package org.valr.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User {
    private String userId;
    private String username;
    private String password;
}