package com.logitrack.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logitrack.infrastructure.adapter.in.web.dto.ApiResponse;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationEntryPoint Tests")
class JwtAuthenticationEntryPointTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ServletOutputStream outputStream;

    @Mock
    private AuthenticationException authenticationException;

    @InjectMocks
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @BeforeEach
    void setUp() throws IOException {
        // Arrange - Common setup for HTTP response mocking
        when(response.getOutputStream()).thenReturn(outputStream);
        when(authenticationException.getMessage()).thenReturn("Invalid token");
    }

    @Nested
    @DisplayName("Authentication Entry Point Tests")
    class AuthenticationEntryPointTests {

        @Test
        @DisplayName("Should set correct response headers and status")
        void shouldSetCorrectResponseHeadersAndStatus() throws Exception {
            // Arrange - mocks already set up in @BeforeEach

            // Act
            jwtAuthenticationEntryPoint.commence(request, response, authenticationException);

            // Assert
            verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(response).getOutputStream();
        }

        @Test
        @DisplayName("Should write correct API response structure")
        void shouldWriteCorrectApiResponseStructure() throws Exception {
            // Arrange
            ArgumentCaptor<ApiResponse<Void>> apiResponseCaptor = ArgumentCaptor.forClass(ApiResponse.class);
            doNothing().when(objectMapper).writeValue(eq(outputStream), any(ApiResponse.class));

            // Act
            jwtAuthenticationEntryPoint.commence(request, response, authenticationException);

            // Assert
            verify(objectMapper).writeValue(eq(outputStream), apiResponseCaptor.capture());

            ApiResponse<Void> capturedResponse = apiResponseCaptor.getValue();
            assertThat(capturedResponse).isNotNull();
            assertThat(capturedResponse.isSuccess()).isFalse();
            assertThat(capturedResponse.getMessage()).isEqualTo("Unauthorized - Authentication required");
            assertThat(capturedResponse.getData()).isNull();

            // Verify error details
            ApiResponse.ErrorDetails errorDetails = capturedResponse.getError();
            assertThat(errorDetails).isNotNull();
            assertThat(errorDetails.getCode()).isEqualTo("UNAUTHORIZED");
            assertThat(errorDetails.getDescription()).isEqualTo("Please provide a valid authentication token");
        }

        @Test
        @DisplayName("Should handle authentication exception with specific message")
        void shouldHandleAuthenticationExceptionWithSpecificMessage() throws Exception {
            // Arrange
            String specificMessage = "JWT token expired";
            when(authenticationException.getMessage()).thenReturn(specificMessage);
            ArgumentCaptor<ApiResponse<Void>> apiResponseCaptor = ArgumentCaptor.forClass(ApiResponse.class);

            // Act
            jwtAuthenticationEntryPoint.commence(request, response, authenticationException);

            // Assert
            verify(objectMapper).writeValue(eq(outputStream), apiResponseCaptor.capture());

            ApiResponse<Void> capturedResponse = apiResponseCaptor.getValue();
            assertThat(capturedResponse.getMessage()).isEqualTo("Unauthorized - Authentication required");

        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle IOException when writing response")
        void shouldHandleIOExceptionWhenWritingResponse() throws Exception {
            // Arrange
            IOException ioException = new IOException("Failed to write response");
            doThrow(ioException).when(objectMapper).writeValue(eq(outputStream), any(ApiResponse.class));

            // Act & Assert
            assertThatThrownBy(() ->
                    jwtAuthenticationEntryPoint.commence(request, response, authenticationException))
                    .isInstanceOf(IOException.class)
                    .hasMessage("Failed to write response");

            // Verify that response setup was attempted
            verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should handle IOException when getting output stream")
        void shouldHandleIOExceptionWhenGettingOutputStream() throws Exception {
            // Arrange
            IOException ioException = new IOException("Cannot get output stream");
            when(response.getOutputStream()).thenThrow(ioException);

            // Act & Assert
            assertThatThrownBy(() ->
                    jwtAuthenticationEntryPoint.commence(request, response, authenticationException))
                    .isInstanceOf(IOException.class)
                    .hasMessage("Cannot get output stream");

            // Verify that response setup was attempted
            verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should handle authentication exception with null message")
        void shouldHandleAuthenticationExceptionWithNullMessage() throws Exception {
            // Arrange
            when(authenticationException.getMessage()).thenReturn(null);
            ArgumentCaptor<ApiResponse<Void>> apiResponseCaptor = ArgumentCaptor.forClass(ApiResponse.class);

            // Act
            jwtAuthenticationEntryPoint.commence(request, response, authenticationException);

            // Assert
            verify(objectMapper).writeValue(eq(outputStream), apiResponseCaptor.capture());

            ApiResponse<Void> capturedResponse = apiResponseCaptor.getValue();
            assertThat(capturedResponse).isNotNull();
            assertThat(capturedResponse.isSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("Response Format Tests")
    class ResponseFormatTests {

        @Test
        @DisplayName("Should always return JSON content type")
        void shouldAlwaysReturnJsonContentType() throws Exception {
            // Arrange - mocks already set up

            // Act
            jwtAuthenticationEntryPoint.commence(request, response, authenticationException);

            // Assert
            verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        }

        @Test
        @DisplayName("Should always return 401 status code")
        void shouldAlwaysReturn401StatusCode() throws Exception {
            // Arrange - mocks already set up

            // Act
            jwtAuthenticationEntryPoint.commence(request, response, authenticationException);

            // Assert
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(response, never()).setStatus(HttpServletResponse.SC_OK);
            verify(response, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
        }

        @Test
        @DisplayName("Should create consistent error response structure")
        void shouldCreateConsistentErrorResponseStructure() throws Exception {
            // Arrange
            ArgumentCaptor<ApiResponse<Void>> apiResponseCaptor = ArgumentCaptor.forClass(ApiResponse.class);

            // Act
            jwtAuthenticationEntryPoint.commence(request, response, authenticationException);

            // Assert
            verify(objectMapper).writeValue(eq(outputStream), apiResponseCaptor.capture());

            ApiResponse<Void> capturedResponse = apiResponseCaptor.getValue();

            // Verify response structure
            assertThat(capturedResponse.isSuccess()).isFalse();
            assertThat(capturedResponse.getMessage()).isNotEmpty();
            assertThat(capturedResponse.getData()).isNull();
            assertThat(capturedResponse.getError()).isNotNull();

            // Verify error details structure
            ApiResponse.ErrorDetails error = capturedResponse.getError();
            assertThat(error.getCode()).isNotEmpty();
            assertThat(error.getDescription()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Integration Behavior Tests")
    class IntegrationBehaviorTests {

        @Test
        @DisplayName("Should complete authentication entry point flow successfully")
        void shouldCompleteAuthenticationEntryPointFlowSuccessfully() throws Exception {
            // Arrange - all mocks set up for happy path

            // Act
            jwtAuthenticationEntryPoint.commence(request, response, authenticationException);

            // Assert - Verify complete flow execution
            verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(response).getOutputStream();
            verify(objectMapper).writeValue(eq(outputStream), any(ApiResponse.class));
        }

        @Test
        @DisplayName("Should use injected ObjectMapper for serialization")
        void shouldUseInjectedObjectMapperForSerialization() throws Exception {
            // Arrange - mocks already set up

            // Act
            jwtAuthenticationEntryPoint.commence(request, response, authenticationException);

            // Assert
            verify(objectMapper, times(1)).writeValue(eq(outputStream), any(ApiResponse.class));
        }

        @Test
        @DisplayName("Should not modify request object")
        void shouldNotModifyRequestObject() throws Exception {
            // Arrange - mocks already set up

            // Act
            jwtAuthenticationEntryPoint.commence(request, response, authenticationException);

            // Assert - Request should not be modified
            verifyNoInteractions(request);
        }
    }

    @Nested
    @DisplayName("Security Response Tests")
    class SecurityResponseTests {

        @Test
        @DisplayName("Should not expose sensitive information in response")
        void shouldNotExposeSensitiveInformationInResponse() throws Exception {
            // Arrange
            String sensitiveMessage = "Database connection failed: password=secret123";
            when(authenticationException.getMessage()).thenReturn(sensitiveMessage);
            ArgumentCaptor<ApiResponse<Void>> apiResponseCaptor = ArgumentCaptor.forClass(ApiResponse.class);

            // Act
            jwtAuthenticationEntryPoint.commence(request, response, authenticationException);

            // Assert
            verify(objectMapper).writeValue(eq(outputStream), apiResponseCaptor.capture());

            ApiResponse<Void> capturedResponse = apiResponseCaptor.getValue();

            // Response should contain generic message, not the sensitive exception message
            assertThat(capturedResponse.getMessage()).isEqualTo("Unauthorized - Authentication required");
            assertThat(capturedResponse.getError().getDescription()).isEqualTo("Please provide a valid authentication token");

            // Should not contain sensitive information
            String responseString = capturedResponse.toString();
            assertThat(responseString).doesNotContain("password");
            assertThat(responseString).doesNotContain("secret123");
        }

        @Test
        @DisplayName("Should provide consistent error code for all authentication failures")
        void shouldProvideConsistentErrorCodeForAllAuthenticationFailures() throws Exception {
            // Arrange
            String[] exceptionMessages = {
                    "JWT token expired",
                    "Invalid signature",
                    "Token not found",
                    "Malformed token",
                    null
            };

            for (String message : exceptionMessages) {
                // Arrange - Reset mocks for each iteration
                reset(objectMapper);
                ArgumentCaptor<ApiResponse<Void>> apiResponseCaptor = ArgumentCaptor.forClass(ApiResponse.class);
                when(authenticationException.getMessage()).thenReturn(message);

                // Act
                jwtAuthenticationEntryPoint.commence(request, response, authenticationException);

                // Assert
                verify(objectMapper, times(1)).writeValue(eq(outputStream), apiResponseCaptor.capture());
                ApiResponse<Void> response = apiResponseCaptor.getValue();

                assertThat(response.getError().getCode()).isEqualTo("UNAUTHORIZED");
            }
        }
    }
}
