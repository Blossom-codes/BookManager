package com.balancee.BookManager.service.impl;

import com.balancee.BookManager.dto.LoginResponse;
import com.balancee.BookManager.repository.UserRepository;
import com.balancee.BookManager.dto.LoginRequest;
import com.balancee.BookManager.dto.ResponseDto;
import com.balancee.BookManager.dto.user.UserRequestDto;
import com.balancee.BookManager.entity.User;
import com.balancee.BookManager.service.UserService;
import com.balancee.BookManager.utils.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    @Value("${app.jwtTokenExpiration}")
    private long expiryTime; // 24 hours in milliseconds


    @Override
    public ResponseDto login(LoginRequest request) {
        ResponseDto responseDto = new ResponseDto();
        try {
            LoginResponse loginResponse = new LoginResponse();
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // Load user details
            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            Optional<User> user = userRepository.findByUsername(request.getUsername());
            Map<String, Object> claims = new HashMap<>();
            // Add custom claims
            if (user.isPresent()) {
                User loggedUser = user.get();
                claims.put("id", loggedUser.getId());
                claims.put("authorities", List.of(loggedUser.getAuthorities()));
                claims.put("email", loggedUser.getEmail());
                claims.put("username", loggedUser.getUsername());
                claims.put("fullName", loggedUser.getFirstName() + " " + loggedUser.getLastName());
                // Generate JWT token
                final String token = jwtUtil.generateToken(userDetails, claims);

                loginResponse.setUserId(loggedUser.getId());
                loginResponse.setUsername(loggedUser.getUsername());
                loginResponse.setEmail(loggedUser.getEmail());
                loginResponse.setFirstName(loggedUser.getFirstName());
                loginResponse.setLastName(loggedUser.getLastName());
                loginResponse.setUserRoles(loggedUser.getAuthorities());
                loginResponse.setPermissions(List.of(""));
                loginResponse.setBearerToken(token);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                loginResponse.setExpiryDate(LocalDateTime.now().plus(expiryTime, ChronoUnit.MILLIS).format(formatter));

                LocalDateTime lastLoginDate = loggedUser.getLastLoginDate();

                if (lastLoginDate == null) {
                    loggedUser.setLastLoginDate(LocalDateTime.now());
                    loginResponse.setLastLoginDate(LocalDateTime.now().format(formatter));
                }
                loggedUser.setStatus(Status.ACTIVE.name());
                loginResponse.setLastLoginDate(loggedUser.getLastLoginDate().format(formatter));

                userRepository.save(loggedUser);
            }

            responseDto.setResponseCode(ResponseCodes.SUCCESS);
            responseDto.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.SUCCESS));
            responseDto.setInfo(loginResponse);
        } catch (BadCredentialsException e) {
            responseDto.setResponseCode(ResponseCodes.SYSTEM_ERROR);
            responseDto.setResponseMessage("Invalid email or password");
        } catch (UsernameNotFoundException e) {
            responseDto.setResponseCode(ResponseCodes.SYSTEM_ERROR);
            responseDto.setResponseMessage("User not found");
        } catch (Exception ex) {
            ex.printStackTrace();
            responseDto.setResponseCode(ResponseCodes.SYSTEM_ERROR);
            responseDto.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.SYSTEM_ERROR));
        }
        return responseDto;
    }

    @Override
    public ResponseDto register(UserRequestDto request) {
        ResponseDto response = new ResponseDto();
        response.setResponseCode(ResponseCodes.FAILED);
        response.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.FAILED));
        try {

            if (userRepository.existsByEmail(request.getEmail())) {
                response.setResponseCode(ResponseCodes.USER_EXISTS);
                response.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.USER_EXISTS));
                return response;
            }
            if (userRepository.existsByUsername(request.getUsername())) {
                response.setResponseCode(ResponseCodes.USERNAME_EXISTS);
                response.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.USERNAME_EXISTS));
                return response;
            }

            User user = new User();
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setAuthorities(Roles.ROLE_USER.name());
//            user.setJoinedOn(LocalDateTime.now());

            userRepository.save(user);
            response.setResponseCode(ResponseCodes.SUCCESS);
            response.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.REGISTER_SUCCESS));

        } catch (Exception e) {
            e.printStackTrace();
            response.setResponseCode(ResponseCodes.SYSTEM_ERROR);
            response.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.ERROR));
        }

        return response;
    }
}
