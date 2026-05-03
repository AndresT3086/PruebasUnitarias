package com.logitrack.infrastructure.adapter.in.web;

import com.logitrack.domain.exception.InvalidPackageDataException;
import com.logitrack.domain.exception.InvalidStateTransitionException;
import com.logitrack.domain.exception.PackageNotFoundException;
import com.logitrack.infrastructure.adapter.in.web.dto.ApiResponse;
import com.logitrack.infrastructure.adapter.in.web.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

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
        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
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
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
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
        assertEquals(409, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Invalid transition", response.getBody().getMessage());
        assertEquals("INVALID_STATE_TRANSITION", response.getBody().getError().getCode());
    }


    @Test
    void shouldHandleAccessDenied() {
        // Arrange
        AccessDeniedException ex = new AccessDeniedException("Denied");

        // Act
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleAccessDenied(ex);

        // Assert
        assertEquals(403, response.getStatusCode().value());
        assertNotNull(response.getBody());
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
        assertEquals(401, response.getStatusCode().value());
        assertNotNull(response.getBody());
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
        assertEquals(500, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_ERROR", response.getBody().getError().getCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle validation exceptions with field errors")
    void shouldHandleValidationExceptionsWithFieldErrors() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/v1/packages");

        MethodArgumentNotValidException ex = buildValidationException();

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleValidationExceptions(ex, request);

        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
        assertFalse(response.getBody().getFieldErrors().isEmpty());
    }

    @Test
    @DisplayName("Should handle validation exceptions with global errors")
    void shouldHandleValidationExceptionsWithGlobalErrors() throws Exception {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/v1/packages");

        MethodArgumentNotValidException ex = buildGlobalErrorValidationException();

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleValidationExceptions(ex, request);

        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
        assertFalse(response.getBody().getFieldErrors().isEmpty());
    }

    // Helpers — agregar al final de la clase de test
    private MethodArgumentNotValidException buildValidationException() throws Exception {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "command");
        bindingResult.addError(new FieldError(
                "command", "recipientEmail", "invalid@",
                false, null, null, "Invalid email format"));
        MethodParameter param = new MethodParameter(
                Object.class.getMethod("toString"), -1);
        return new MethodArgumentNotValidException(param, bindingResult);
    }

    private MethodArgumentNotValidException buildGlobalErrorValidationException() throws Exception {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "command");
        bindingResult.addError(new ObjectError("command", "Object level error"));
        MethodParameter param = new MethodParameter(
                Object.class.getMethod("toString"), -1);
        return new MethodArgumentNotValidException(param, bindingResult);
    }
}
