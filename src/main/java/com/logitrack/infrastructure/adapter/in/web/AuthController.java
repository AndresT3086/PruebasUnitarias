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

    private record UserEntry(String password, List<String> roles) {}

    private static final Map<String, UserEntry> USER_STORE = Map.of(
            "admin@logitrack.com",    new UserEntry("admin123",    List.of("ADMIN")),
            "operator@logitrack.com", new UserEntry("operator123", List.of("OPERATOR")),
            "viewer@logitrack.com",   new UserEntry("viewer123",   List.of("VIEWER"))
    );

    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate user and get JWT token")
    public ResponseEntity<Map<String, String>> login(
            @Valid @RequestBody LoginRequest request) {

        UserEntry entry = USER_STORE.get(request.email);
        if (entry == null || !entry.password().equals(request.password)) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid credentials"));
        }

        List<String> roles = entry.roles();
        String token = tokenProvider.generateToken(request.email, roles);

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
