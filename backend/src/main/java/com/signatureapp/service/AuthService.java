package com.signatureapp.service;

import com.signatureapp.dto.AuthResponse;
import com.signatureapp.dto.LoginRequest;
import com.signatureapp.dto.RegisterRequest;
import com.signatureapp.model.AuditAction;
import com.signatureapp.model.User;
import com.signatureapp.repository.UserRepository;
import com.signatureapp.security.JwtService;
import com.signatureapp.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            AuditService auditService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.auditService = auditService;
    }

    public AuthResponse register(RegisterRequest request, String ipAddress) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }
        var user = new User();
        user.setName(request.name());
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(user);
        auditService.record(null, user, AuditAction.REGISTER, "User registered", ipAddress);
        return response(user);
    }

    public AuthResponse login(LoginRequest request, String ipAddress) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password())
        );
        var user = userRepository.findByEmail(request.email().toLowerCase()).orElseThrow();
        auditService.record(null, user, AuditAction.LOGIN, "User logged in", ipAddress);
        return response(user);
    }

    public User currentUser(UserPrincipal principal) {
        return principal.getUser();
    }

    private AuthResponse response(User user) {
        String token = jwtService.generateToken(new UserPrincipal(user));
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail());
    }
}
