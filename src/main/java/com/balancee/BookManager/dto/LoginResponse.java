package com.balancee.BookManager.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class LoginResponse {
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String username;
    private String address;
    private String bearerToken;
    private String lastLoginDate;
    private String status;
    private String expiryDate;
    private String userRoles;
    private List<String> permissions;

}
