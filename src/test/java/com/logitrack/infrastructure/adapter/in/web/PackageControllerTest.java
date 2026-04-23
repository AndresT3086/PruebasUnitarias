package com.logitrack.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logitrack.application.dto.*;
import com.logitrack.application.service.PackageService;
import com.logitrack.domain.model.PackageStatus;
import com.logitrack.infrastructure.adapter.in.web.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PackageController Tests")
class PackageControllerTest {

    @Mock
    private PackageService packageService;

    @InjectMocks
    private PackageController packageController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private CreatePackageCommand createPackageCommand;
    private PackageResponse packageResponse;
    private AddLocationCommand addLocationCommand;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(packageController).build();
        objectMapper = new ObjectMapper();

        // Setup test data with correct structure
        createPackageCommand = CreatePackageCommand.builder()
                .recipientName("John Doe")
                .recipientEmail("john@test.com")
                .recipientPhone("+1234567890")
                .street("123 Main St")
                .city("New York")
                .country("USA")
                .postalCode("10001")
                .weight(2.5)
                .height(10.0)
                .width(15.0)
                .depth(20.0)
                .notes("Test package")
                .build();

        // Create nested DTOs correctly
        PackageResponse.AddressDto address = PackageResponse.AddressDto.builder()
                .street("123 Main St")
                .city("New York")
                .country("USA")
                .postalCode("10001")
                .fullAddress("123 Main St, New York, USA 10001")
                .build();

        PackageResponse.RecipientDto recipient = PackageResponse.RecipientDto.builder()
                .name("John Doe")
                .email("john@test.com")
                .phone("+1234567890")
                .address(address)
                .build();

        PackageResponse.DimensionsDto dimensions = PackageResponse.DimensionsDto.builder()
                .height(10.0)
                .width(15.0)
                .depth(20.0)
                .volume(3000.0)
                .build();

        packageResponse = PackageResponse.builder()
                .id("LT-123456789")
                .recipient(recipient)
                .dimensions(dimensions)
                .weight(2.5)
                .status(PackageStatus.CREATED)
                .notes("Test package")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        addLocationCommand = AddLocationCommand.builder()
                .packageId("LT-123456789")
                .city("Philadelphia")
                .country("USA")
                .description("In transit")
                .latitude(39.9526)
                .longitude(-75.1652)
                .build();
    }

    @Nested
    @DisplayName("Create Package Tests")
    class CreatePackageTests {

        @Test
        @DisplayName("Should create package successfully")
        void shouldCreatePackageSuccessfully() throws Exception {
            // Arrange
            when(packageService.createPackage(any(CreatePackageCommand.class))).thenReturn(packageResponse);

            // Act & Assert
            mockMvc.perform(post("/api/v1/packages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createPackageCommand)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Package created successfully"))
                    .andExpect(jsonPath("$.data.id").value("LT-123456789"))
                    .andExpect(jsonPath("$.data.recipient.name").value("John Doe"))
                    .andExpect(jsonPath("$.data.recipient.email").value("john@test.com"))
                    .andExpect(jsonPath("$.data.status").value("CREATED"));

            verify(packageService).createPackage(any(CreatePackageCommand.class));
        }

        @Test
        @DisplayName("Should handle validation errors for invalid create package request")
        void shouldHandleValidationErrorsForInvalidCreatePackageRequest() throws Exception {
            // Arrange - Create command with missing required fields
            String invalidJson = "{}";

            // Act & Assert
            mockMvc.perform(post("/api/v1/packages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());

            verify(packageService, never()).createPackage(any());
        }

        @Test
        @DisplayName("Should return created package response using direct method call")
        void shouldReturnCreatedPackageResponseUsingDirectMethodCall() {
            // Arrange
            when(packageService.createPackage(createPackageCommand)).thenReturn(packageResponse);

            // Act
            ResponseEntity<ApiResponse<PackageResponse>> response = packageController.createPackage(createPackageCommand);

            // Assert
            assertThat(response.getStatusCode().value()).isEqualTo(201);
            assertThat(response.getBody().isSuccess()).isTrue();
            assertThat(response.getBody().getMessage()).isEqualTo("Package created successfully");
            assertThat(response.getBody().getData()).isEqualTo(packageResponse);

            verify(packageService).createPackage(createPackageCommand);
        }
    }

    @Nested
    @DisplayName("Get Package Tests")
    class GetPackageTests {

        @Test
        @DisplayName("Should get package by ID successfully")
        void shouldGetPackageByIdSuccessfully() throws Exception {
            // Arrange
            String packageId = "LT-123456789";
            when(packageService.getByIdOrThrow(packageId)).thenReturn(packageResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/packages/{packageId}", packageId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Package retrieved successfully"))
                    .andExpect(jsonPath("$.data.id").value(packageId))
                    .andExpect(jsonPath("$.data.recipient.name").value("John Doe"));

            verify(packageService).getByIdOrThrow(packageId);
        }

        @Test
        @DisplayName("Should return package response using direct method call")
        void shouldReturnPackageResponseUsingDirectMethodCall() {
            // Arrange
            String packageId = "LT-123456789";
            when(packageService.getByIdOrThrow(packageId)).thenReturn(packageResponse);

            // Act
            ResponseEntity<ApiResponse<PackageResponse>> response = packageController.getPackage(packageId);

            // Assert
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody().isSuccess()).isTrue();
            assertThat(response.getBody().getMessage()).isEqualTo("Package retrieved successfully");
            assertThat(response.getBody().getData().getId()).isEqualTo(packageId);

            verify(packageService).getByIdOrThrow(packageId);
        }
    }

    @Nested
    @DisplayName("Update Status Tests")
    class UpdateStatusTests {

        @Test
        @DisplayName("Should update package status successfully")
        void shouldUpdatePackageStatusSuccessfully() throws Exception {
            // Arrange
            String packageId = "LT-123456789";
            PackageStatus newStatus = PackageStatus.IN_TRANSIT;
            PackageResponse updatedResponse = PackageResponse.builder()
                    .id(packageId)
                    .recipient(packageResponse.getRecipient())
                    .status(newStatus)
                    .weight(packageResponse.getWeight())
                    .build();

            when(packageService.updateStatus(packageId, newStatus)).thenReturn(updatedResponse);

            // Act & Assert
            mockMvc.perform(put("/api/v1/packages/{packageId}/status", packageId)
                            .param("status", newStatus.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Package status updated successfully"))
                    .andExpect(jsonPath("$.data.id").value(packageId))
                    .andExpect(jsonPath("$.data.status").value("IN_TRANSIT"));

            verify(packageService).updateStatus(packageId, newStatus);
        }
    }

    @Nested
    @DisplayName("Get By Status Tests")
    class GetByStatusTests {

        @Test
        @DisplayName("Should get packages by status successfully")
        void shouldGetPackagesByStatusSuccessfully() throws Exception {
            // Arrange
            PackageStatus status = PackageStatus.IN_TRANSIT;
            List<PackageResponse> packages = Arrays.asList(packageResponse);
            when(packageService.findByStatus(status)).thenReturn(packages);

            // Act & Assert
            mockMvc.perform(get("/api/v1/packages/status/{status}", status))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Found 1 packages with status IN_TRANSIT"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].id").value("LT-123456789"));

            verify(packageService).findByStatus(status);
        }

        @Test
        @DisplayName("Should return empty list when no packages found with status")
        void shouldReturnEmptyListWhenNoPackagesFoundWithStatus() {
            // Arrange
            PackageStatus status = PackageStatus.DELIVERED;
            List<PackageResponse> emptyList = Arrays.asList();
            when(packageService.findByStatus(status)).thenReturn(emptyList);

            // Act
            ResponseEntity<ApiResponse<List<PackageResponse>>> response = packageController.getByStatus(status);

            // Assert
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody().getMessage()).isEqualTo("Found 0 packages with status DELIVERED");
            assertThat(response.getBody().getData()).isEmpty();

            verify(packageService).findByStatus(status);
        }
    }

    @Nested
    @DisplayName("Delete Package Tests")
    class DeletePackageTests {

        @Test
        @DisplayName("Should delete package successfully")
        void shouldDeletePackageSuccessfully() throws Exception {
            // Arrange
            String packageId = "LT-123456789";
            doNothing().when(packageService).deletePackage(packageId);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/packages/{packageId}", packageId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Package deleted successfully"))
                    .andExpect(jsonPath("$.data").doesNotExist());

            verify(packageService).deletePackage(packageId);
        }

        @Test
        @DisplayName("Should return success response using direct method call")
        void shouldReturnSuccessResponseUsingDirectMethodCall() {
            // Arrange
            String packageId = "LT-123456789";
            doNothing().when(packageService).deletePackage(packageId);

            // Act
            ResponseEntity<ApiResponse<Void>> response = packageController.deletePackage(packageId);

            // Assert
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody().isSuccess()).isTrue();
            assertThat(response.getBody().getMessage()).isEqualTo("Package deleted successfully");
            assertThat(response.getBody().getData()).isNull();

            verify(packageService).deletePackage(packageId);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle malformed JSON in create package")
        void shouldHandleMalformedJsonInCreatePackage() throws Exception {
            // Arrange
            String malformedJson = "{ recipientName: 'John', recipientEmail: }";

            // Act & Assert
            mockMvc.perform(post("/api/v1/packages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(malformedJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle invalid package status in update")
        void shouldHandleInvalidPackageStatusInUpdate() throws Exception {
            // Arrange
            String packageId = "LT-123456789";

            // Act & Assert
            mockMvc.perform(put("/api/v1/packages/{packageId}/status", packageId)
                            .param("status", "INVALID_STATUS"))
                    .andExpect(status().isBadRequest());
        }
    }
}
