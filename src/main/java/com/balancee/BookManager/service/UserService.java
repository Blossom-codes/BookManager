package com.balancee.BookManager.service;

import com.balancee.BookManager.dto.LoginRequest;
import com.balancee.BookManager.dto.LoginResponse;
import com.balancee.BookManager.dto.ResponseDto;
import com.balancee.BookManager.dto.user.UserInfo;
import com.balancee.BookManager.dto.user.UserRequestDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

public interface UserService {
    ResponseDto login(LoginRequest request);
    ResponseDto register(UserRequestDto request, boolean admin);
    void makeAdmin(UUID id);
    UserInfo validateToken(HttpServletRequest request);
}
