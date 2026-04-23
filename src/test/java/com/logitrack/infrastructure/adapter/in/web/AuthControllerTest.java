package com.logitrack.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logitrack.infrastructure.config.JwtTokenProvider;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("Login Success Tests")
    class LoginSuccessTests {

        @Test
        @DisplayName("Should authenticate admin user successfully")
        void shouldAuthenticateAdminUserSuccessfully() throws Exception {
            // Arrange
            String email = "admin@logitrack.com";
            String password = "admin123";
            String expectedToken = "jwt-token-admin";
            AuthController.LoginRequest loginRequest = new AuthController.LoginRequest(email, password);

            when(tokenProvider.generateToken(eq(email), eq(List.of("ADMIN")))).thenReturn(expectedToken);

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value(expectedToken))
                    .andExpect(jsonPath("$.type").value("Bearer"))
                    .andExpect(jsonPath("$.email").value(email))
                    .andExpect(jsonPath("$.roles").value("ADMIN"));

            verify(tokenProvider).generateToken(email, List.of("ADMIN"));
        }

        @Test
        @DisplayName("Should authenticate operator user successfully")
        void shouldAuthenticateOperatorUserSuccessfully() throws Exception {
            // Arrange
            String email = "operator@logitrack.com";
            String password = "operator123";
            String expectedToken = "jwt-token-operator";
            AuthController.LoginRequest loginRequest = new AuthController.LoginRequest(email, password);

            when(tokenProvider.generateToken(eq(email), eq(List.of("OPERATOR")))).thenReturn(expectedToken);

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value(expectedToken))
                    .andExpect(jsonPath("$.type").value("Bearer"))
                    .andExpect(jsonPath("$.email").value(email))
                    .andExpect(jsonPath("$.roles").value("OPERATOR"));

            verify(tokenProvider).generateToken(email, List.of("OPERATOR"));
        }

        @Test
        @DisplayName("Should authenticate viewer user successfully")
        void shouldAuthenticateViewerUserSuccessfully() throws Exception {
            // Arrange
            String email = "viewer@logitrack.com";
            String password = "viewer123";
            String expectedToken = "jwt-token-viewer";
            AuthController.LoginRequest loginRequest = new AuthController.LoginRequest(email, password);

            when(tokenProvider.generateToken(eq(email), eq(List.of("VIEWER")))).thenReturn(expectedToken);

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value(expectedToken))
                    .andExpect(jsonPath("$.type").value("Bearer"))
                    .andExpect(jsonPath("$.email").value(email))
                    .andExpect(jsonPath("$.roles").value("VIEWER"));

            verify(tokenProvider).generateToken(email, List.of("VIEWER"));
        }
    }

    @Nested
    @DisplayName("Login Failure Tests")
    class LoginFailureTests {

        @Test
        @DisplayName("Should return 401 for invalid email")
        void shouldReturn401ForInvalidEmail() throws Exception {
            // Arrange
            String invalidEmail = "invalid@logitrack.com";
            String password = "admin123";
            AuthController.LoginRequest loginRequest = new AuthController.LoginRequest(invalidEmail, password);

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid credentials"));

            verify(tokenProvider, never()).generateToken(anyString(), anyList());
        }

        @Test
        @DisplayName("Should return 401 for invalid password")
        void shouldReturn401ForInvalidPassword() throws Exception {
            // Arrange
            String email = "admin@logitrack.com";
            String invalidPassword = "wrongpassword";
            AuthController.LoginRequest loginRequest = new AuthController.LoginRequest(email, invalidPassword);

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid credentials"));

            verify(tokenProvider, never()).generateToken(anyString(), anyList());
        }

        @Test
        @DisplayName("Should return 401 for valid email with wrong password")
        void shouldReturn401ForValidEmailWithWrongPassword() throws Exception {
            // Arrange
            String email = "operator@logitrack.com";
            String wrongPassword = "admin123"; // Correct for admin, wrong for operator
            AuthController.LoginRequest loginRequest = new AuthController.LoginRequest(email, wrongPassword);

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid credentials"));

            verify(tokenProvider, never()).generateToken(anyString(), anyList());
        }

        @Test
        @DisplayName("Should return 401 for empty credentials")
        void shouldReturn401ForEmptyCredentials() throws Exception {
            // Arrange
            AuthController.LoginRequest loginRequest = new AuthController.LoginRequest("", "");

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid credentials"));

            verify(tokenProvider, never()).generateToken(anyString(), anyList());
        }

        @Test
        @DisplayName("Should return 401 for null credentials")
        void shouldReturn401ForNullCredentials() throws Exception {
            // Arrange
            AuthController.LoginRequest loginRequest = new AuthController.LoginRequest(null, null);

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid credentials"));

            verify(tokenProvider, never()).generateToken(anyString(), anyList());
        }
    }

    @Nested
    @DisplayName("Direct Method Tests")
    class DirectMethodTests {

        @Test
        @DisplayName("Should return success response for admin login")
        void shouldReturnSuccessResponseForAdminLogin() {
            // Arrange
            String email = "admin@logitrack.com";
            String password = "admin123";
            String expectedToken = "test-jwt-token";
            AuthController.LoginRequest loginRequest = new AuthController.LoginRequest(email, password);

            when(tokenProvider.generateToken(email, List.of("ADMIN"))).thenReturn(expectedToken);

            // Act
            ResponseEntity<Map<String, String>> response = authController.login(loginRequest);

            // Assert
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).containsEntry("token", expectedToken);
            assertThat(response.getBody()).containsEntry("type", "Bearer");
            assertThat(response.getBody()).containsEntry("email", email);
            assertThat(response.getBody()).containsEntry("roles", "ADMIN");

            verify(tokenProvider).generateToken(email, List.of("ADMIN"));
        }

        @Test
        @DisplayName("Should return error response for invalid credentials")
        void shouldReturnErrorResponseForInvalidCredentials() {
            // Arrange
            String email = "invalid@test.com";
            String password = "wrongpassword";
            AuthController.LoginRequest loginRequest = new AuthController.LoginRequest(email, password);

            // Act
            ResponseEntity<Map<String, String>> response = authController.login(loginRequest);

            // Assert
            assertThat(response.getStatusCode().value()).isEqualTo(401);
            assertThat(response.getBody()).containsEntry("error", "Invalid credentials");

            verify(tokenProvider, never()).generateToken(anyString(), anyList());
        }
    }

    @Nested
    @DisplayName("Token Generation Tests")
    class TokenGenerationTests {

        @Test
        @DisplayName("Should generate token with correct parameters for each user type")
        void shouldGenerateTokenWithCorrectParametersForEachUserType() {
            // Arrange
            String adminToken = "admin-token";
            String operatorToken = "operator-token";
            String viewerToken = "viewer-token";

            when(tokenProvider.generateToken("admin@logitrack.com", List.of("ADMIN"))).thenReturn(adminToken);
            when(tokenProvider.generateToken("operator@logitrack.com", List.of("OPERATOR"))).thenReturn(operatorToken);
            when(tokenProvider.generateToken("viewer@logitrack.com", List.of("VIEWER"))).thenReturn(viewerToken);

            // Act
            ResponseEntity<Map<String, String>> adminResponse = authController.login(
                    new AuthController.LoginRequest("admin@logitrack.com", "admin123"));
            ResponseEntity<Map<String, String>> operatorResponse = authController.login(
                    new AuthController.LoginRequest("operator@logitrack.com", "operator123"));
            ResponseEntity<Map<String, String>> viewerResponse = authController.login(
                    new AuthController.LoginRequest("viewer@logitrack.com", "viewer123"));

            // Assert
            ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<List<String>> rolesCaptor = ArgumentCaptor.forClass(List.class);

            verify(tokenProvider, times(3)).generateToken(emailCaptor.capture(), rolesCaptor.capture());

            List<String> capturedEmails = emailCaptor.getAllValues();
            List<List<String>> capturedRoles = rolesCaptor.getAllValues();

            assertThat(capturedEmails).containsExactly(
                    "admin@logitrack.com",
                    "operator@logitrack.com",
                    "viewer@logitrack.com"
            );

            assertThat(capturedRoles).containsExactly(
                    List.of("ADMIN"),
                    List.of("OPERATOR"),
                    List.of("VIEWER")
            );

            assertThat(adminResponse.getBody()).containsEntry("token", adminToken);
            assertThat(operatorResponse.getBody()).containsEntry("token", operatorToken);
            assertThat(viewerResponse.getBody()).containsEntry("token", viewerToken);
        }
    }

    @Nested
    @DisplayName("Request Validation Tests")
    class RequestValidationTests {

        @Test
        @DisplayName("Should handle malformed JSON request")
        void shouldHandleMalformedJsonRequest() throws Exception {
            // Arrange
            String malformedJson = "{ email: 'test@test.com', password: }";

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(malformedJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle empty request body")
        void shouldHandleEmptyRequestBody() throws Exception {
            // Arrange & Act & Assert
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle missing content type")
        void shouldHandleMissingContentType() throws Exception {
            // Arrange
            AuthController.LoginRequest loginRequest = new AuthController.LoginRequest("test@test.com", "password");

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/login")
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnsupportedMediaType());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should be case sensitive for email addresses")
        void shouldBeCaseSensitiveForEmailAddresses() throws Exception {
            // Arrange
            String uppercaseEmail = "ADMIN@LOGITRACK.COM";
            String password = "admin123";
            AuthController.LoginRequest loginRequest = new AuthController.LoginRequest(uppercaseEmail, password);

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid credentials"));
        }

        @Test
        @DisplayName("Should handle whitespace in credentials")
        void shouldHandleWhitespaceInCredentials() throws Exception {
            // Arrange
            String emailWithSpaces = " admin@logitrack.com ";
            String passwordWithSpaces = " admin123 ";
            AuthController.LoginRequest loginRequest = new AuthController.LoginRequest(emailWithSpaces, passwordWithSpaces);

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid credentials"));
        }

        @Test
        @DisplayName("Should handle very long credentials")
        void shouldHandleVeryLongCredentials() throws Exception {
            // Arrange
            String longEmail = "a".repeat(1000) + "@logitrack.com";
            String longPassword = "b".repeat(1000);
            AuthController.LoginRequest loginRequest = new AuthController.LoginRequest(longEmail, longPassword);

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid credentials"));
        }
    }

    @Nested
    @DisplayName("Response Format Tests")
    class ResponseFormatTests {

        @Test
        @DisplayName("Should return all required fields in success response")
        void shouldReturnAllRequiredFieldsInSuccessResponse() throws Exception {
            // Arrange
            String email = "admin@logitrack.com";
            String password = "admin123";
            String expectedToken = "test-token-12345";
            AuthController.LoginRequest loginRequest = new AuthController.LoginRequest(email, password);

            when(tokenProvider.generateToken(email, List.of("ADMIN"))).thenReturn(expectedToken);

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.type").exists())
                    .andExpect(jsonPath("$.email").exists())
                    .andExpect(jsonPath("$.roles").exists())
                    .andExpect(jsonPath("$.token").value(expectedToken))
                    .andExpect(jsonPath("$.type").value("Bearer"))
                    .andExpect(jsonPath("$.email").value(email))
                    .andExpect(jsonPath("$.roles").value("ADMIN"));
        }

        @Test
        @DisplayName("Should return only error field in failure response")
        void shouldReturnOnlyErrorFieldInFailureResponse() throws Exception {
            // Arrange
            AuthController.LoginRequest loginRequest = new AuthController.LoginRequest("invalid@test.com", "wrong");

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.error").value("Invalid credentials"))
                    .andExpect(jsonPath("$.token").doesNotExist())
                    .andExpect(jsonPath("$.type").doesNotExist())
                    .andExpect(jsonPath("$.email").doesNotExist())
                    .andExpect(jsonPath("$.roles").doesNotExist());
        }
    }
}
