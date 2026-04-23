package com.logitrack.infrastructure.adapter.in.web;

import com.logitrack.domain.exception.InvalidPackageDataException;
import com.logitrack.domain.exception.InvalidStateTransitionException;
import com.logitrack.domain.exception.PackageNotFoundException;
import com.logitrack.infrastructure.adapter.in.web.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandlePackageNotFound() {
        // Arrange
        PackageNotFoundException ex = new PackageNotFoundException("Package not found");

        // Act
        ResponseEntity<ApiResponse<Void>> response = handler.handlePackageNotFound(ex, request);

        // Assert
        assertEquals(404, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Package not found:Package not found", response.getBody().getMessage());
        assertEquals("PACKAGE_NOT_FOUND", response.getBody().getError().getCode());
    }

    @Test
    void shouldHandleInvalidPackageData() {
        // Arrange
        InvalidPackageDataException ex = new InvalidPackageDataException("Invalid data");

        // Act
        ResponseEntity<ApiResponse<Void>> response = handler.handleInvalidPackageData(ex);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid data", response.getBody().getMessage());
        assertEquals("INVALID_PACKAGE_DATA", response.getBody().getError().getCode());
    }

    @Test
    void shouldHandleInvalidStateTransition() {
        // Arrange
        InvalidStateTransitionException ex =
                new InvalidStateTransitionException("Invalid transition");

        // Act
        ResponseEntity<ApiResponse<Void>> response = handler.handleInvalidStateTransition(ex);

        // Assert
        assertEquals(409, response.getStatusCodeValue());
        assertEquals("Invalid transition", response.getBody().getMessage());
        assertEquals("INVALID_STATE_TRANSITION", response.getBody().getError().getCode());
    }


    @Test
    void shouldHandleTypeMismatch() {
        // Arrange
        MethodArgumentTypeMismatchException ex =
                new MethodArgumentTypeMismatchException("abc", Integer.class, "id", null, null);

        // Act
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleTypeMismatch(ex);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().getMessage().contains("Invalid value"));
        assertEquals("TYPE_MISMATCH", response.getBody().getError().getCode());
    }

    @Test
    void shouldHandleAccessDenied() {
        // Arrange
        AccessDeniedException ex = new AccessDeniedException("Denied");

        // Act
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleAccessDenied(ex);

        // Assert
        assertEquals(403, response.getStatusCodeValue());
        assertEquals("ACCESS_DENIED", response.getBody().getError().getCode());
    }

    @Test
    void shouldHandleBadCredentials() {
        // Arrange
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");

        // Act
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleBadCredentials(ex);

        // Assert
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("BAD_CREDENTIALS", response.getBody().getError().getCode());
    }

    @Test
    void shouldHandleGenericException() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/error");
        Exception ex = new RuntimeException("Unexpected");

        // Act
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleGenericException(ex, request);

        // Assert
        assertEquals(500, response.getStatusCodeValue());
        assertEquals("INTERNAL_ERROR", response.getBody().getError().getCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }
}
