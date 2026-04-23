package com.logitrack.application.service;

import com.logitrack.application.dto.AddLocationCommand;
import com.logitrack.application.dto.CreatePackageCommand;
import com.logitrack.application.dto.PackageResponse;
import com.logitrack.application.factory.PackageFactory;
import com.logitrack.domain.event.DomainEvent;
import com.logitrack.domain.exception.PackageNotFoundException;
import com.logitrack.domain.model.*;
import com.logitrack.domain.model.Package;
import com.logitrack.domain.port.in.AddLocationUseCase;
import com.logitrack.domain.port.in.CreatePackageUseCase;
import com.logitrack.domain.port.out.EventPublisher;
import com.logitrack.domain.port.out.LocationService;
import com.logitrack.domain.port.out.PackageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PackageService Tests")
class PackageServiceImplTest {

    @Mock
    private PackageRepository packageRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private LocationService locationService;

    @Mock
    private PackageFactory packageFactory;

    @InjectMocks
    private PackageServiceImpl packageService;

    private Package testPackage;
    private CreatePackageCommand createCommand;
    private AddLocationCommand addLocationCommand;
    private LocationService.LocationInfo locationInfo;

    @BeforeEach
    void setUp() {
        // Arrange - Common test data setup
        Recipient.Address address = Recipient.Address.builder()
                .street("123 Test St")
                .city("Test City")
                .state("Test State")
                .country("Test Country")
                .postalCode("12345")
                .build();

        Recipient recipient = Recipient.builder()
                .name("John Doe")
                .email("john@test.com")
                .phone("+1234567890")
                .address(address)
                .build();

        testPackage = Package.builder()
                .id(PackageId.of("LT-TEST12345"))
                .recipient(recipient)
                .dimensions(Dimensions.of(20, 15, 10))
                .weight(Weight.ofKilograms(2.5))
                .notes("Test package")
                .build();

        createCommand = CreatePackageCommand.builder()
                .recipientName("John Doe")
                .recipientEmail("john@test.com")
                .recipientPhone("+1234567890")
                .street("123 Test St")
                .city("Test City")
                .state("Test State")
                .country("Test Country")
                .postalCode("12345")
                .height(20.0)
                .width(15.0)
                .depth(10.0)
                .weight(2.5)
                .notes("Test package")
                .build();

        addLocationCommand = AddLocationCommand.builder()
                .packageId("LT-TEST12345")
                .city("New City")
                .country("New Country")
                .description("Package in transit")
                .latitude(40.7128)
                .longitude(-74.0060)
                .build();

        locationInfo = new LocationService.LocationInfo(
                "Test City",
                "Test Country",
                "Test State",
                40.7128,
                -74.0060,
                "UTC"
        );
    }

    @Nested
    @DisplayName("Create Package Tests")
    class CreatePackageTests {

        @Test
        @DisplayName("Should create package successfully with location info")
        void shouldCreatePackageSuccessfullyWithLocationInfo() {
            // Arrange
            when(locationService.getLocationInfo("Test City", "Test Country"))
                    .thenReturn(Optional.of(locationInfo));
            when(packageFactory.createPackage(createCommand))
                    .thenReturn(testPackage);
            when(packageRepository.save(testPackage))
                    .thenReturn(testPackage);
            doNothing().when(eventPublisher).publish(anyString(), any(DomainEvent.class));

            // Act
            PackageResponse result = packageService.createPackage(createCommand);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("LT-TEST12345");
            assertThat(result.getRecipient().getName()).isEqualTo("John Doe");
            assertThat(result.getStatus()).isEqualTo(PackageStatus.CREATED);

            verify(locationService).getLocationInfo("Test City", "Test Country");
            verify(packageFactory).createPackage(createCommand);
            verify(packageRepository).save(testPackage);
            verify(eventPublisher, atLeastOnce()).publish(eq("package-events"), any(DomainEvent.class));
        }

        @Test
        @DisplayName("Should create package successfully without location info")
        void shouldCreatePackageSuccessfullyWithoutLocationInfo() {
            // Arrange
            when(locationService.getLocationInfo("Test City", "Test Country"))
                    .thenReturn(Optional.empty());
            when(packageFactory.createPackage(createCommand))
                    .thenReturn(testPackage);
            when(packageRepository.save(testPackage))
                    .thenReturn(testPackage);
            doNothing().when(eventPublisher).publish(anyString(), any(DomainEvent.class));

            // Act
            PackageResponse result = packageService.createPackage(createCommand);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("LT-TEST12345");

            verify(locationService).getLocationInfo("Test City", "Test Country");
            verify(packageFactory).createPackage(createCommand);
            verify(packageRepository).save(testPackage);
        }

        @Test
        @DisplayName("Should create package from use case command")
        void shouldCreatePackageFromUseCaseCommand() {
            // Arrange
            var useCaseCommand = new CreatePackageUseCase.CreatePackageCommand(
                    "John Doe", "john@test.com", "+1234567890",
                    "123 Test St", "Test City", "Test State", "Test Country", "12345",
                    20.0, 15.0, 10.0, 2.5, "Test package"
            );

            when(locationService.getLocationInfo(anyString(), anyString()))
                    .thenReturn(Optional.of(locationInfo));
            when(packageFactory.createPackage(any(CreatePackageCommand.class)))
                    .thenReturn(testPackage);
            when(packageRepository.save(any(Package.class)))
                    .thenReturn(testPackage);
            when(packageRepository.findById("LT-TEST12345"))
                    .thenReturn(Optional.of(testPackage));

            // Act
            Package result = packageService.createPackage(useCaseCommand);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId().getValue()).isEqualTo("LT-TEST12345");

            verify(packageFactory).createPackage(any(CreatePackageCommand.class));
            verify(packageRepository).save(testPackage);
            verify(packageRepository).findById("LT-TEST12345");
        }
    }

    @Nested
    @DisplayName("Update Package Status Tests")
    class UpdatePackageStatusTests {

        @Test
        @DisplayName("Should update package status successfully")
        void shouldUpdatePackageStatusSuccessfully() {
            // Arrange
            when(packageRepository.findByIdAndNotDeleted("LT-TEST12345"))
                    .thenReturn(Optional.of(testPackage));
            when(packageRepository.save(testPackage))
                    .thenReturn(testPackage);
            doNothing().when(eventPublisher).publish(anyString(), any(DomainEvent.class));

            // Act
            PackageResponse result = packageService.updateStatus("LT-TEST12345", PackageStatus.IN_TRANSIT);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("LT-TEST12345");

            verify(packageRepository).findByIdAndNotDeleted("LT-TEST12345");
            verify(packageRepository).save(testPackage);
            verify(eventPublisher, atLeastOnce()).publish(eq("package-events"), any(DomainEvent.class));
        }

        @Test
        @DisplayName("Should throw exception when package not found for status update")
        void shouldThrowExceptionWhenPackageNotFoundForStatusUpdate() {
            // Arrange
            when(packageRepository.findByIdAndNotDeleted("INVALID_ID"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> packageService.updateStatus("INVALID_ID", PackageStatus.IN_TRANSIT))
                    .isInstanceOf(PackageNotFoundException.class)
                    .hasMessageContaining("Package not found:INVALID_ID");

            verify(packageRepository).findByIdAndNotDeleted("INVALID_ID");
            verify(packageRepository, never()).save(any());
            verify(eventPublisher, never()).publish(anyString(), any());
        }

        @Test
        @DisplayName("Should update package status via use case")
        void shouldUpdatePackageStatusViaUseCase() {
            // Arrange
            when(packageRepository.findById("LT-TEST12345"))
                    .thenReturn(Optional.of(testPackage));

            // Act
            Package result = packageService.updatePackageStatus("LT-TEST12345", PackageStatus.IN_TRANSIT);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId().getValue()).isEqualTo("LT-TEST12345");

            verify(packageRepository).findById("LT-TEST12345");
        }
    }

    @Nested
    @DisplayName("Add Location Tests")
    class AddLocationTests {

        @Test
        @DisplayName("Should add location with coordinates successfully")
        void shouldAddLocationWithCoordinatesSuccessfully() {
            // Arrange
            when(packageRepository.findByIdAndNotDeleted("LT-TEST12345"))
                    .thenReturn(Optional.of(testPackage));
            when(locationService.getLocationByCoordinates(40.7128, -74.0060))
                    .thenReturn(Optional.of(locationInfo));
            when(packageRepository.save(testPackage))
                    .thenReturn(testPackage);
            doNothing().when(eventPublisher).publish(anyString(), any(DomainEvent.class));

            // Act
            PackageResponse result = packageService.addLocation(addLocationCommand);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("LT-TEST12345");

            verify(packageRepository).findByIdAndNotDeleted("LT-TEST12345");
            verify(locationService).getLocationByCoordinates(40.7128, -74.0060);
            verify(packageRepository).save(testPackage);
            verify(eventPublisher, atLeastOnce()).publish(eq("package-events"), any(DomainEvent.class));
        }

        @Test
        @DisplayName("Should add location without coordinates successfully")
        void shouldAddLocationWithoutCoordinatesSuccessfully() {
            // Arrange
            AddLocationCommand commandWithoutCoordinates = AddLocationCommand.builder()
                    .packageId("LT-TEST12345")
                    .city("New City")
                    .country("New Country")
                    .description("Package in transit")
                    .build();

            when(packageRepository.findByIdAndNotDeleted("LT-TEST12345"))
                    .thenReturn(Optional.of(testPackage));
            when(locationService.validateLocation("New City", "New Country"))
                    .thenReturn(true);
            when(packageRepository.save(testPackage))
                    .thenReturn(testPackage);
            doNothing().when(eventPublisher).publish(anyString(), any(DomainEvent.class));

            // Act
            PackageResponse result = packageService.addLocation(commandWithoutCoordinates);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("LT-TEST12345");

            verify(packageRepository).findByIdAndNotDeleted("LT-TEST12345");
            verify(locationService).validateLocation("New City", "New Country");
            verify(packageRepository).save(testPackage);
        }

        @Test
        @DisplayName("Should add location via use case command")
        void shouldAddLocationViaUseCaseCommand() {
            // Arrange
            var useCaseCommand = new AddLocationUseCase.AddLocationCommand(
                    "LT-TEST12345", "New City", "New Country", "In transit", 40.7128, -74.0060
            );

            when(packageRepository.findByIdAndNotDeleted("LT-TEST12345"))
                    .thenReturn(Optional.of(testPackage));
            when(locationService.getLocationByCoordinates(anyDouble(), anyDouble()))
                    .thenReturn(Optional.of(locationInfo));
            when(packageRepository.save(any(Package.class)))
                    .thenReturn(testPackage);
            when(packageRepository.findById("LT-TEST12345"))
                    .thenReturn(Optional.of(testPackage));

            // Act
            Package result = packageService.addLocation(useCaseCommand);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId().getValue()).isEqualTo("LT-TEST12345");

            verify(packageRepository).findById("LT-TEST12345");
        }

        @Test
        @DisplayName("Should throw exception when package not found for location add")
        void shouldThrowExceptionWhenPackageNotFoundForLocationAdd() {
            // Arrange
            when(packageRepository.findByIdAndNotDeleted("INVALID_ID"))
                    .thenReturn(Optional.empty());

            AddLocationCommand invalidCommand = AddLocationCommand.builder()
                    .packageId("INVALID_ID")
                    .city("City")
                    .country("Country")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> packageService.addLocation(invalidCommand))
                    .isInstanceOf(PackageNotFoundException.class)
                    .hasMessageContaining("Package not found:INVALID_ID");
        }
    }

    @Nested
    @DisplayName("Find Package Tests")
    class FindPackageTests {

        @Test
        @DisplayName("Should find package by ID successfully")
        void shouldFindPackageByIdSuccessfully() {
            // Arrange
            when(packageRepository.findByIdAndNotDeleted("LT-TEST12345"))
                    .thenReturn(Optional.of(testPackage));

            // Act
            Optional<PackageResponse> result = packageService.findById("LT-TEST12345");

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo("LT-TEST12345");
            assertThat(result.get().getRecipient().getName()).isEqualTo("John Doe");

            verify(packageRepository).findByIdAndNotDeleted("LT-TEST12345");
        }

        @Test
        @DisplayName("Should return empty when package not found")
        void shouldReturnEmptyWhenPackageNotFound() {
            // Arrange
            when(packageRepository.findByIdAndNotDeleted("INVALID_ID"))
                    .thenReturn(Optional.empty());

            // Act
            Optional<PackageResponse> result = packageService.findById("INVALID_ID");

            // Assert
            assertThat(result).isEmpty();

            verify(packageRepository).findByIdAndNotDeleted("INVALID_ID");
        }

        @Test
        @DisplayName("Should get package or throw exception")
        void shouldGetPackageOrThrowException() {
            // Arrange
            when(packageRepository.findByIdAndNotDeleted("LT-TEST12345"))
                    .thenReturn(Optional.of(testPackage));

            // Act
            PackageResponse result = packageService.getByIdOrThrow("LT-TEST12345");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("LT-TEST12345");

            verify(packageRepository).findByIdAndNotDeleted("LT-TEST12345");
        }

        @Test
        @DisplayName("Should throw exception when getting non-existent package")
        void shouldThrowExceptionWhenGettingNonExistentPackage() {
            // Arrange
            when(packageRepository.findByIdAndNotDeleted("INVALID_ID"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> packageService.getByIdOrThrow("INVALID_ID"))
                    .isInstanceOf(PackageNotFoundException.class)
                    .hasMessageContaining("Package not found:INVALID_ID");
        }

        @Test
        @DisplayName("Should get package via use case")
        void shouldGetPackageViaUseCase() {
            // Arrange
            when(packageRepository.findByIdAndNotDeleted("LT-TEST12345"))
                    .thenReturn(Optional.of(testPackage));

            // Act
            Optional<Package> result = packageService.getPackage("LT-TEST12345");

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getId().getValue()).isEqualTo("LT-TEST12345");

            verify(packageRepository).findByIdAndNotDeleted("LT-TEST12345");
        }
    }

    @Nested
    @DisplayName("Search Package Tests")
    class SearchPackageTests {

        @Test
        @DisplayName("Should search packages with criteria")
        void shouldSearchPackagesWithCriteria() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<Package> packages = Arrays.asList(testPackage);
            Page<Package> packagePage = new PageImpl<>(packages, pageable, 1);

            PackageService.SearchCriteria criteria = new PackageService.SearchCriteria(
                    "John", "john@test.com", PackageStatus.CREATED, null, null, false
            );

            when(packageRepository.search(any(PackageRepository.SearchCriteria.class), eq(pageable)))
                    .thenReturn(packagePage);

            // Act
            Page<PackageResponse> result = packageService.searchPackages(criteria, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo("LT-TEST12345");

            verify(packageRepository).search(any(PackageRepository.SearchCriteria.class), eq(pageable));
        }

        @Test
        @DisplayName("Should search packages with date filters")
        void shouldSearchPackagesWithDateFilters() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            PackageService.SearchCriteria criteria = new PackageService.SearchCriteria(
                    null, null, null,
                    "2023-01-01T00:00:00", "2023-12-31T23:59:59", false
            );

            when(packageRepository.search(any(PackageRepository.SearchCriteria.class), eq(pageable)))
                    .thenReturn(new PageImpl<>(Arrays.asList(testPackage)));

            // Act
            Page<PackageResponse> result = packageService.searchPackages(criteria, pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(packageRepository).search(any(PackageRepository.SearchCriteria.class), eq(pageable));
        }

        @Test
        @DisplayName("Should find packages by status")
        void shouldFindPackagesByStatus() {
            // Arrange
            List<Package> packages = Arrays.asList(testPackage);
            when(packageRepository.findByStatus(PackageStatus.CREATED))
                    .thenReturn(packages);

            // Act
            List<PackageResponse> result = packageService.findByStatus(PackageStatus.CREATED);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo("LT-TEST12345");
            assertThat(result.get(0).getStatus()).isEqualTo(PackageStatus.CREATED);

            verify(packageRepository).findByStatus(PackageStatus.CREATED);
        }

        @Test
        @DisplayName("Should get packages by status via use case")
        void shouldGetPackagesByStatusViaUseCase() {
            // Arrange
            List<Package> packages = Arrays.asList(testPackage);
            when(packageRepository.findByStatus(PackageStatus.CREATED))
                    .thenReturn(packages);

            // Act
            List<Package> result = packageService.getPackagesByStatus(PackageStatus.CREATED);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId().getValue()).isEqualTo("LT-TEST12345");

            verify(packageRepository).findByStatus(PackageStatus.CREATED);
        }
    }

    @Nested
    @DisplayName("Delete Package Tests")
    class DeletePackageTests {

        @Test
        @DisplayName("Should soft delete package successfully")
        void shouldSoftDeletePackageSuccessfully() {
            // Arrange
            when(packageRepository.findByIdAndNotDeleted("LT-TEST12345"))
                    .thenReturn(Optional.of(testPackage));
            when(packageRepository.save(testPackage))
                    .thenReturn(testPackage);

            // Act
            packageService.deletePackage("LT-TEST12345");

            // Assert
            verify(packageRepository).findByIdAndNotDeleted("LT-TEST12345");
            verify(packageRepository).save(testPackage);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent package")
        void shouldThrowExceptionWhenDeletingNonExistentPackage() {
            // Arrange
            when(packageRepository.findByIdAndNotDeleted("INVALID_ID"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> packageService.deletePackage("INVALID_ID"))
                    .isInstanceOf(PackageNotFoundException.class)
                    .hasMessageContaining("Package not found:INVALID_ID");

            verify(packageRepository).findByIdAndNotDeleted("INVALID_ID");
            verify(packageRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Event Publishing Tests")
    class EventPublishingTests {

        @Test
        @DisplayName("Should handle event publishing errors gracefully")
        void shouldHandleEventPublishingErrorsGracefully() {
            // Arrange
            when(locationService.getLocationInfo(anyString(), anyString()))
                    .thenReturn(Optional.of(locationInfo));
            when(packageFactory.createPackage(createCommand))
                    .thenReturn(testPackage);
            when(packageRepository.save(testPackage))
                    .thenReturn(testPackage);
            doThrow(new RuntimeException("Kafka error"))
                    .when(eventPublisher).publish(anyString(), any(DomainEvent.class));

            // Act & Assert - Should not throw exception
            assertThatCode(() -> packageService.createPackage(createCommand))
                    .doesNotThrowAnyException();

            verify(eventPublisher, atLeastOnce()).publish(eq("package-events"), any(DomainEvent.class));
        }
    }

    @Nested
    @DisplayName("Location Validation Tests")
    class LocationValidationTests {

        @Test
        @DisplayName("Should validate location when adding without coordinates")
        void shouldValidateLocationWhenAddingWithoutCoordinates() {
            // Arrange
            AddLocationCommand commandWithoutCoordinates = AddLocationCommand.builder()
                    .packageId("LT-TEST12345")
                    .city("Invalid City")
                    .country("Invalid Country")
                    .description("Test location")
                    .build();

            when(packageRepository.findByIdAndNotDeleted("LT-TEST12345"))
                    .thenReturn(Optional.of(testPackage));
            when(locationService.validateLocation("Invalid City", "Invalid Country"))
                    .thenReturn(false);
            when(packageRepository.save(testPackage))
                    .thenReturn(testPackage);

            // Act
            PackageResponse result = packageService.addLocation(commandWithoutCoordinates);

            // Assert
            assertThat(result).isNotNull();
            verify(locationService).validateLocation("Invalid City", "Invalid Country");
        }
    }
}
