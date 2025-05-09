package com.balancee.BookManager.controller;

import com.balancee.BookManager.dto.user.UserInfo;
import com.balancee.BookManager.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class BaseController {

    @Autowired
    private JwtUtil jwtUtil;

    protected UserInfo validateToken(HttpServletRequest request) {
        UserInfo userInfo = new UserInfo();
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // remove "Bearer "
            } else {
                return null;
            }

            Claims claims = jwtUtil.extractAllClaims(token);

            userInfo.setId(Long.valueOf(claims.get("id").toString()));
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
}
