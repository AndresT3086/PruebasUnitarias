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

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .error(ApiResponse.ErrorDetails.builder()
                                .code(ex.getCode())
                                .description("The requested package could not be found")
                                .build())
                        .build());
    }

    @ExceptionHandler(InvalidPackageDataException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidPackageData(
            InvalidPackageDataException ex) {
        log.warn("Invalid package data: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .error(ApiResponse.ErrorDetails.builder()
                                .code(ex.getCode())
                                .description("The provided package data is invalid")
                                .build())
                        .build());
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidStateTransition(
            InvalidStateTransitionException ex) {
        log.warn("Invalid state transition: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .error(ApiResponse.ErrorDetails.builder()
                                .code(ex.getCode())
                                .description("The requested state transition is not allowed")
                                .build())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        log.warn("Validation failed for request to {}", request.getRequestURI());

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getAllErrors().stream()
                .map(error -> {
                    String fieldName = error instanceof FieldError ?
                            ((FieldError) error).getField() : error.getObjectName();
                    return ErrorResponse.FieldError.builder()
                            .field(fieldName)
                            .message(error.getDefaultMessage())
                            .rejectedValue(error instanceof FieldError ?
                                    ((FieldError) error).getRejectedValue() : null)
                            .build();
                })
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Validation failed")
                .errorCode("VALIDATION_ERROR")
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        String message = String.format("Invalid value '%s' for parameter '%s'",
                ex.getValue(), ex.getName());
        log.warn("Type mismatch: {}", message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(message)
                        .error(ApiResponse.ErrorDetails.builder()
                                .code("TYPE_MISMATCH")
                                .description("The provided value has an incorrect type")
                                .build())
                        .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Access denied")
                        .error(ApiResponse.ErrorDetails.builder()
                                .code("ACCESS_DENIED")
                                .description("You don't have permission to access this resource")
                                .build())
                        .build());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
            BadCredentialsException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Authentication failed")
                        .error(ApiResponse.ErrorDetails.builder()
                                .code("BAD_CREDENTIALS")
                                .description("Invalid username or password")
                                .build())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error processing request to {}",
                request.getRequestURI(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("An unexpected error occurred")
                        .error(ApiResponse.ErrorDetails.builder()
                                .code("INTERNAL_ERROR")
                                .description("Please try again later or contact support")
                                .build())
                        .build());
    }
}
