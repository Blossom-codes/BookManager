package com.balancee.BookManager.service;

import com.balancee.BookManager.dto.LoginRequest;
import com.balancee.BookManager.dto.LoginResponse;
import com.balancee.BookManager.dto.ResponseDto;
import com.balancee.BookManager.dto.user.UserRequestDto;

public interface UserService {
    ResponseDto login(LoginRequest request);
    ResponseDto register(UserRequestDto request);
}
