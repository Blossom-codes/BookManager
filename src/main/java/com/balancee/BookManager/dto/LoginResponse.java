package com.balancee.BookManager.dto;

import lombok.Data;

import java.util.List;

@Data
public class LoginResponse {
    private Long userId;
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
