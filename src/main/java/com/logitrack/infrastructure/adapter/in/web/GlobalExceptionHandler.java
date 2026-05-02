package com.logitrack.infrastructure.adapter.in.web;

import com.logitrack.domain.exception.InvalidPackageDataException;
import com.logitrack.domain.exception.InvalidStateTransitionException;
import com.logitrack.domain.exception.PackageNotFoundException;
import com.logitrack.infrastructure.adapter.in.web.dto.ApiResponse;
import com.logitrack.infrastructure.adapter.in.web.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PackageNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handlePackageNotFound(
            PackageNotFoundException ex, HttpServletRequest request) {
        log.warn("Package not found: {}", ex.getMessage());
        return errorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), ex.getCode(),
                "The requested package could not be found");
    }

    @ExceptionHandler(InvalidPackageDataException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidPackageData(
            InvalidPackageDataException ex) {
        log.warn("Invalid package data: {}", ex.getMessage());
        return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getCode(),
                "The provided package data is invalid");
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidStateTransition(
            InvalidStateTransitionException ex) {
        log.warn("Invalid state transition: {}", ex.getMessage());
        return errorResponse(HttpStatus.CONFLICT, ex.getMessage(), ex.getCode(),
                "The requested state transition is not allowed");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        log.warn("Validation failed for request to {}", request.getRequestURI());

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError fe) {
                        return ErrorResponse.FieldError.builder()
                                .field(fe.getField())
                                .message(fe.getDefaultMessage())
                                .rejectedValue(fe.getRejectedValue())
                                .build();
                    }
                    return ErrorResponse.FieldError.builder()
                            .field(error.getObjectName())
                            .message(error.getDefaultMessage())
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .message("Validation failed")
                        .errorCode("VALIDATION_ERROR")
                        .status(HttpStatus.BAD_REQUEST.value())
                        .path(request.getRequestURI())
                        .fieldErrors(fieldErrors)
                        .build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        String message = String.format("Invalid value '%s' for parameter '%s'",
                ex.getValue(), ex.getName());
        log.warn("Type mismatch: {}", message);
        return errorResponse(HttpStatus.BAD_REQUEST, message, "TYPE_MISMATCH",
                "The provided value has an incorrect type");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return errorResponse(HttpStatus.FORBIDDEN, "Access denied", "ACCESS_DENIED",
                "You don't have permission to access this resource");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
            BadCredentialsException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return errorResponse(HttpStatus.UNAUTHORIZED, "Authentication failed", "BAD_CREDENTIALS",
                "Invalid username or password");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error processing request to {}",
                request.getRequestURI(), ex);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred",
                "INTERNAL_ERROR", "Please try again later or contact support");
    }

    private ResponseEntity<ApiResponse<Void>> errorResponse(
            HttpStatus status, String message, String code, String description) {
        return ResponseEntity.status(status)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(message)
                        .error(ApiResponse.ErrorDetails.builder()
                                .code(code)
                                .description(description)
                                .build())
                        .build());
    }
}
