package com.balancee.BookManager.controller;

import com.balancee.BookManager.dto.LoginRequest;
import com.balancee.BookManager.dto.ResponseDto;
import com.balancee.BookManager.dto.user.EditRequestDto;
import com.balancee.BookManager.dto.user.UserInfo;
import com.balancee.BookManager.dto.user.UserRequestDto;
import com.balancee.BookManager.service.UserService;
import com.balancee.BookManager.utils.LocaleHandler;
import com.balancee.BookManager.utils.ResponseCodes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;


    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;


    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLogin_Success() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("Test@1234");

        ResponseDto expectedResponse = new ResponseDto();
        expectedResponse.setResponseCode(ResponseCodes.SUCCESS);
        expectedResponse.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.SUCCESS));

        when(userService.login(request)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ResponseDto> response = userController.login(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(userService).login(request);
    }

    @Test
    void testRegisterUser_Success() {
        // Arrange
        UserRequestDto request = new UserRequestDto();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setUsername("johndoe");
        request.setPassword("Password@123");

        ResponseDto expectedResponse = new ResponseDto();
        expectedResponse.setResponseCode(ResponseCodes.SUCCESS);
        expectedResponse.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.REGISTER_SUCCESS));

        when(userService.register(request,false)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ResponseDto> response = userController.registerUser(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(userService).register(request,false);
    }

    @Test
    void testUpdateUser_Success() {
        // Arrange
        EditRequestDto request = new EditRequestDto();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setUsername("doeJohn");
        request.setPassword("!!Password123");

        UUID userId = UUID.randomUUID();

        ResponseDto expectedResponse = new ResponseDto();
        expectedResponse.setResponseCode(ResponseCodes.SUCCESS);
        expectedResponse.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.UPDATE_SUCCESS));

        // Spy the controller to mock validateToken
        UserController controllerSpy = Mockito.spy(userController);

        UserInfo mockUserInfo = new UserInfo();
        mockUserInfo.setId(userId); // assuming UserInfo has an id field

        doReturn(mockUserInfo).when(controllerSpy).validateToken(httpServletRequest);
        when(userService.updateProfile(request, userId)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ResponseDto> response = controllerSpy.update(httpServletRequest, httpServletResponse, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(userService).updateProfile(request, userId);
    }


    @Test
    void testLogin_Exception() {
        LoginRequest request = new LoginRequest();
        request.setUsername("failuser");
        request.setPassword("wrongpass");

        when(userService.login(request)).thenThrow(new RuntimeException("Service unavailable"));

        ResponseEntity<ResponseDto> response = userController.login(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(ResponseCodes.ERROR, response.getBody().getResponseCode());
    }

    @Test
    void testRegisterUser_Exception() {
        UserRequestDto request = new UserRequestDto();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("jane@example.com");
        request.setUsername("janedoe");
        request.setPassword("Invalid");

        when(userService.register(request, false)).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<ResponseDto> response = userController.registerUser(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
