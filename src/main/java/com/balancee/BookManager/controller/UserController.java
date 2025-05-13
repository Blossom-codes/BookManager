package com.balancee.BookManager.controller;


import com.balancee.BookManager.dto.LoginRequest;
import com.balancee.BookManager.dto.ResponseDto;
import com.balancee.BookManager.dto.user.EditRequestDto;
import com.balancee.BookManager.dto.user.UserInfo;
import com.balancee.BookManager.dto.user.UserRequestDto;
import com.balancee.BookManager.service.UserService;
import com.balancee.BookManager.utils.LocaleHandler;
import com.balancee.BookManager.utils.LoggingUtils;
import com.balancee.BookManager.utils.ResponseCodes;
import com.balancee.BookManager.utils.Roles;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
public class UserController extends BaseController{

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
            summary = "Registers a new user as admin",
            description = "This endpoint allows users to register as admins by providing their details."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin registered successfully",
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
            summary = "Update user details",
            description = "This endpoint allows users to update their details."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "details updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or update failure",
                    content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/update")
    public ResponseEntity<ResponseDto> update(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                              @Valid @RequestBody EditRequestDto request) {
        ResponseDto response = new ResponseDto();
        response.setResponseCode(ResponseCodes.ERROR);
        response.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.ERROR));

        UserInfo userInfo = new UserInfo();
        try {
            userInfo = this.validateToken(httpServletRequest);
            if (userInfo == null) {
                LoggingUtils.DebugInfo("An error occurred: unauthorized access");
                response.setResponseCode(ResponseCodes.USER_NOT_AUTHORIZED);
                response.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.USER_NOT_AUTHORIZED));
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            response = userService.updateProfile(request, userInfo.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }
    }

    @Operation(
            summary = "Activate admin account",
            description = ""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User made an admin successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or activation failure",
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