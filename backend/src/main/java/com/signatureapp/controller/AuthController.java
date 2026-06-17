package com.signatureapp.controller;

import com.signatureapp.dto.AuthResponse;
import com.signatureapp.dto.LoginRequest;
import com.signatureapp.dto.RegisterRequest;
import com.signatureapp.service.AuthService;
import com.signatureapp.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
        return authService.register(request, RequestUtil.clientIp(servletRequest));
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return authService.login(request, RequestUtil.clientIp(servletRequest));
    }
}
