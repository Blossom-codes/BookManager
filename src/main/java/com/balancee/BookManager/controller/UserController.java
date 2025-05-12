package com.balancee.BookManager.controller;


import com.balancee.BookManager.dto.LoginRequest;
import com.balancee.BookManager.dto.ResponseDto;
import com.balancee.BookManager.dto.user.UserRequestDto;
import com.balancee.BookManager.service.UserService;
import com.balancee.BookManager.utils.LocaleHandler;
import com.balancee.BookManager.utils.LoggingUtils;
import com.balancee.BookManager.utils.ResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController{

    private final UserService userService;

    @Operation(
            summary = "User Login",
            description = "Authenticates a user and returns a response containing login details."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid login request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/auth/login")
    public ResponseEntity<ResponseDto> login(@RequestBody LoginRequest loginRequest) {
        ResponseDto response = new ResponseDto();
        response.setResponseCode(ResponseCodes.ERROR);
        response.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.ERROR));

        try {
            LoggingUtils.DebugInfo("Login attempt - Username: " + loginRequest.getUsername());

            response = userService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LoggingUtils.DebugInfo("Error during login: " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(
            summary = "Registers a new user",
            description = "This endpoint allows users to register by providing their details."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or registration failure",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/auth/register")
    public ResponseEntity<ResponseDto> registerUser(@Valid @RequestBody UserRequestDto request) {
        ResponseDto response = new ResponseDto();
        try {
            response = userService.register(request, false);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }
    }

    @Operation(
            summary = "Registers a new user",
            description = "This endpoint allows users to register by providing their details."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or registration failure",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/auth/register/admin")
    public ResponseEntity<ResponseDto> registerAsAdmin(@Valid @RequestBody UserRequestDto request) {
        ResponseDto response = new ResponseDto();
        try {
            response = userService.register(request, true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }
    }

    @Operation(
            summary = "Make user an admin",
            description = ""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User made an admin successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or activate failure",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/auth/register/admin/activate")
    public ResponseEntity<?> makeAdmin(@RequestParam("id") UUID id) {
        try {
            userService.makeAdmin(id);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }


}