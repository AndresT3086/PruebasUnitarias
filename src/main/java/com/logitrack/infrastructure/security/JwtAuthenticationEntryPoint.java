package com.logitrack.infrastructure.security;

import org.springframework.security.web.AuthenticationEntryPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logitrack.infrastructure.adapter.in.web.dto.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint  implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        String errorMessage = authException != null ? authException.getMessage() : "No authentication details";
        log.error("Unauthorized access attempt: {}", errorMessage );

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .success(false)
                .message("Unauthorized - Authentication required")
                .error(ApiResponse.ErrorDetails.builder()
                        .code("UNAUTHORIZED")
                        .description("Please provide a valid authentication token")
                        .build())
                .build();

        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }
}
