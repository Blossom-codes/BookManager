package com.balancee.BookManager.service.impl;

import com.balancee.BookManager.dto.EmailDto;
import com.balancee.BookManager.dto.LoginResponse;
import com.balancee.BookManager.dto.user.EditRequestDto;
import com.balancee.BookManager.dto.user.UserInfo;
import com.balancee.BookManager.repository.UserRepository;
import com.balancee.BookManager.dto.LoginRequest;
import com.balancee.BookManager.dto.ResponseDto;
import com.balancee.BookManager.dto.user.UserRequestDto;
import com.balancee.BookManager.entity.User;
import com.balancee.BookManager.service.MailService;
import com.balancee.BookManager.service.UserService;
import com.balancee.BookManager.utils.*;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.h2.util.StringUtils;
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
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final MailService mailService;
    @Value("${app.jwtTokenExpiration}")
    private long expiryTime; // 24 hours in milliseconds
    private static final String userOnboardingMail = "BOOK MANAGER: USER ONBOARDING MAIL";
    private static final String adminOnboardingMail = "BOOK MANAGER: ADMIN ONBOARDING MAIL";


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
                claims.put("authorities", loggedUser.getAuthorities());
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
            responseDto.setResponseCode(ResponseCodes.FAILED);
            responseDto.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.BAD_CREDENTIALS));
        } catch (UsernameNotFoundException e) {
            responseDto.setResponseCode(ResponseCodes.SYSTEM_ERROR);
            responseDto.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.USER_NOT_FOUND));
        } catch (Exception ex) {
            ex.printStackTrace();
            responseDto.setResponseCode(ResponseCodes.SYSTEM_ERROR);
            responseDto.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.SYSTEM_ERROR));
        }
        return responseDto;
    }

    @Override
    public ResponseDto register(UserRequestDto request, boolean admin) {
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
            EmailDto onboardingEmail = new EmailDto();

            if (admin) {
                onboardingEmail.setSubject(adminOnboardingMail);
                onboardingEmail.setRecipient(user.getEmail());
                onboardingEmail.setMessage(adminMessage(user));
            } else {
                onboardingEmail.setSubject(userOnboardingMail);
                onboardingEmail.setRecipient(user.getEmail());
                onboardingEmail.setMessage(message(user));
            }

            mailService.sendHtmlEmailAlert(onboardingEmail);
            response.setResponseCode(ResponseCodes.SUCCESS);
            response.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.REGISTER_SUCCESS));

        } catch (Exception e) {
            e.printStackTrace();
            response.setResponseCode(ResponseCodes.SYSTEM_ERROR);
            response.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.ERROR));
        }

        return response;
    }

    @Override
    public void makeAdmin(UUID id) {
        LoggingUtils.DebugInfo("Trying to make user with id: " + id + " an admin");
        try {
            if (id == null) {
                throw new RuntimeException("An error occurred: Invalid User Id");
            }
            Optional<User> user = userRepository.findById(id);
            if (user.isPresent()) {
                User admin = user.get();
                admin.setAuthorities(Roles.ROLE_ADMIN.name());
                userRepository.save(admin);
                LoggingUtils.DebugInfo("User with id: " + id + " is now an admin");

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String message(User user) {
        return "<html>" +
                "<head>" +
                "  <style>" +
                "    body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }" +
                "    .container { background-color: #ffffff; max-width: 600px; margin: 30px auto; padding: 30px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1); }" +
                "    h2 { color: #2c3e50; }" +
                "    p { color: #555555; font-size: 16px; line-height: 1.5; }" +
                "    .footer { font-size: 12px; color: #999999; margin-top: 20px; text-align: center; }" +
                "    .btn { display: inline-block; padding: 12px 24px; margin-top: 20px; background-color: #1e81b0; color: white !important; text-decoration: none; border-radius: 5px; }" +
                "  </style>" +
                "</head>" +
                "<body>" +
                "  <div class=\"container\">" +
                "    <h2>Welcome to BookManager!</h2>" +
                "    <p>Hi <strong>" + user.getFirstName() + "</strong>,</p>" +
                "    <p>We're excited to have you on board. Your account has been successfully created and you're now part of our growing community.</p>" +
                "    <p>You can now start exploring, saving, and managing books easily.</p>" +
                "    <a class=\"btn\" href=\"http://localhost:8080/bookmanager/api/v1/users/auth/login\">Get Started</a>" +
                "    <p class=\"footer\">If you did not sign up for this service, please ignore this email.</p>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }

    private String adminMessage(User user) {
        return "<html>" +
                "<head>" +
                "  <style>" +
                "    body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }" +
                "    .container { background-color: #ffffff; max-width: 600px; margin: 30px auto; padding: 30px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1); }" +
                "    h2 { color: #2c3e50; }" +
                "    p { color: #555555; font-size: 16px; line-height: 1.5; }" +
                "    .footer { font-size: 12px; color: #999999; margin-top: 20px; text-align: center; }" +
                "    .btn { display: inline-block; padding: 12px 24px; margin-top: 20px; background-color: #1e81b0; color: white !important; text-decoration: none; border-radius: 5px; }" +
                "  </style>" +
                "</head>" +
                "<body>" +
                "  <div class=\"container\">" +
                "    <h2>Welcome to BookManager!</h2>" +
                "    <p>Hi <strong>" + user.getFirstName() + "</strong>,</p>" +
                "    <p>We're excited to have you on board. Your account has been successfully created and you're now part of our growing community.</p>" +
                "    <p>You can now start exploring, saving, and managing books easily as an Admin.</p>" +
                "    <p>Go ahead and click the link to activate your account as an admin, then login.</p>" +
                "    <a class=\"btn\" href=\"http://localhost:8080/bookmanager/api/v1/users/auth/register/admin/activate?id=" + user.getId() + "\">Activate this account</a>" +
                "    <p class=\"footer\">If you did not sign up as an admin for this service, please ignore this email.</p>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }

    @Override
    public UserInfo validateToken(HttpServletRequest request) {
        UserInfo userInfo = new UserInfo();
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // remove "Bearer "
            } else {
                return null;
            }

            Claims claims = jwtUtil.extractAllClaims(token);

            userInfo.setId(UUID.fromString(claims.get("id").toString()));
            userInfo.setAuthorities(claims.get("authorities").toString());
            userInfo.setEmail(claims.get("email").toString());
            userInfo.setUsername(claims.get("username").toString());
            String fullName = claims.get("fullName").toString();
            String[] a = fullName.split(" ");
            String firstName = a[0];
            String lastName = a[1];
            userInfo.setFirstName(firstName);
            userInfo.setLastName(lastName);

        } catch (Exception ex) {
            throw new RuntimeException("An error has occurred: " + ex.getMessage());
        }
        return userInfo;
    }

    @Override
    public ResponseDto updateProfile(EditRequestDto requestDto, UUID id) {
        ResponseDto response = new ResponseDto();
        response.setResponseMessage(ResponseCodes.FAILED);
        response.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.FAILED));

        try {
            Optional<User> optionalUser = userRepository.findById(id);

            if (optionalUser.isEmpty()) {
                LoggingUtils.DebugInfo("User not found for id: " + id);
                return response;
            }

            User user = optionalUser.get();

            if (!StringUtils.isNullOrEmpty(requestDto.getFirstName())) {
                user.setFirstName(requestDto.getFirstName());
            }
            if (!StringUtils.isNullOrEmpty(requestDto.getLastName())) {
                user.setLastName(requestDto.getLastName());
            }
            if (!StringUtils.isNullOrEmpty(requestDto.getUsername())) {
                user.setUsername(requestDto.getUsername());
            }
            if (!StringUtils.isNullOrEmpty(requestDto.getPassword())) {
                user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
            }
            userRepository.save(user);
            response.setResponseCode(ResponseCodes.SUCCESS);
            response.setResponseMessage(LocaleHandler.getMessage(ResponseCodes.UPDATE_SUCCESS));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return response;
    }

}
