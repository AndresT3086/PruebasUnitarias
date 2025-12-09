package com.logitrack.infrastructure.adapter.in.web;

import com.logitrack.infrastructure.config.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate user and get JWT token")
    public ResponseEntity<Map<String, String>> login(
            @Valid @RequestBody LoginRequest request) {

        String token = null;
        List<String> roles = null;

        if ("admin@logitrack.com".equals(request.email) &&
                "admin123".equals(request.password)) {
            roles = List.of("ADMIN");
            token = tokenProvider.generateToken(request.email, roles);
        } else if ("operator@logitrack.com".equals(request.email) &&
                "operator123".equals(request.password)) {
            roles = List.of("OPERATOR");
            token = tokenProvider.generateToken(request.email, roles);
        } else if ("viewer@logitrack.com".equals(request.email) &&
                "viewer123".equals(request.password)) {
            roles = List.of("VIEWER");
            token = tokenProvider.generateToken(request.email, roles);
        } else {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid credentials"));
        }

        log.info("User {} authenticated with roles: {}", request.email, roles);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "type", "Bearer",
                "email", request.email,
                "roles", String.join(",", roles)
        ));
    }

    record LoginRequest(String email, String password) {}

}
