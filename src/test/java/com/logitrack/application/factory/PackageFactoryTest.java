package com.logitrack.application.factory;

import com.logitrack.application.dto.CreatePackageCommand;
import com.logitrack.domain.exception.InvalidPackageDataException;
import com.logitrack.domain.model.*;
import com.logitrack.domain.model.Package;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PackageFactory Tests")
class PackageFactoryTest {

    @InjectMocks
    private PackageFactory packageFactory;

    private CreatePackageCommand validCommand;

    @BeforeEach
    void setUp() {
        // Arrange - Common setup for all tests
        validCommand = CreatePackageCommand.builder()
                .recipientName("John Doe")
                .recipientEmail("john.doe@email.com")
                .recipientPhone("+1234567890")
                .street("123 Main St")
                .city("New York")
                .state("NY")
                .country("USA")
                .postalCode("10001")
                .height(20.0)
                .width(15.0)
                .depth(10.0)
                .weight(2.5)
                .notes("Fragile items")
                .build();
    }

    @Nested
    @DisplayName("Package Creation Tests")
    class PackageCreationTests {

        @Test
        @DisplayName("Should create package with valid command")
        void shouldCreatePackageWithValidCommand() {
            // Arrange - validCommand already set up in @BeforeEach

            // Act
            Package result = packageFactory.createPackage(validCommand);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getId().getValue()).startsWith("LT-");
            assertThat(result.getStatus()).isEqualTo(PackageStatus.CREATED);
            assertThat(result.isDeleted()).isFalse();
            assertThat(result.getCreatedAt()).isNotNull();
            assertThat(result.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should create package with correct recipient information")
        void shouldCreatePackageWithCorrectRecipientInformation() {
            // Arrange - validCommand already set up

            // Act
            Package result = packageFactory.createPackage(validCommand);

            // Assert
            Recipient recipient = result.getRecipient();
            assertThat(recipient).isNotNull();
            assertThat(recipient.getName()).isEqualTo("John Doe");
            assertThat(recipient.getEmail()).isEqualTo("john.doe@email.com");
            assertThat(recipient.getPhone()).isEqualTo("+1234567890");

            Recipient.Address address = recipient.getAddress();
            assertThat(address).isNotNull();
            assertThat(address.getStreet()).isEqualTo("123 Main St");
            assertThat(address.getCity()).isEqualTo("New York");
            assertThat(address.getState()).isEqualTo("NY");
            assertThat(address.getCountry()).isEqualTo("USA");
            assertThat(address.getPostalCode()).isEqualTo("10001");
            assertThat(address.getFullAddress()).contains("123 Main St", "New York", "NY", "USA", "10001");
        }

        @Test
        @DisplayName("Should create package with correct dimensions")
        void shouldCreatePackageWithCorrectDimensions() {
            // Arrange - validCommand already set up

            // Act
            Package result = packageFactory.createPackage(validCommand);

            // Assert
            Dimensions dimensions = result.getDimensions();
            assertThat(dimensions).isNotNull();
            assertThat(dimensions.getHeight().doubleValue()).isEqualTo(20.0);
            assertThat(dimensions.getWidth().doubleValue()).isEqualTo(15.0);
            assertThat(dimensions.getDepth().doubleValue()).isEqualTo(10.0);
            assertThat(dimensions.calculateVolume().doubleValue()).isEqualTo(3000.0);
        }

        @Test
        @DisplayName("Should create package with correct weight")
        void shouldCreatePackageWithCorrectWeight() {
            // Arrange - validCommand already set up

            // Act
            Package result = packageFactory.createPackage(validCommand);

            // Assert
            Weight weight = result.getWeight();
            assertThat(weight).isNotNull();
            assertThat(weight.toKilograms()).isEqualTo(2.5);
            assertThat(weight.toGrams()).isEqualTo(2500.0);
        }

        @Test
        @DisplayName("Should create package with notes")
        void shouldCreatePackageWithNotes() {
            // Arrange - validCommand already set up

            // Act
            Package result = packageFactory.createPackage(validCommand);

            // Assert
            assertThat(result.getNotes()).isEqualTo("Fragile items");
        }

        @Test
        @DisplayName("Should create package without state when notes are null")
        void shouldCreatePackageWithoutStateWhenNotesAreNull() {
            // Arrange
            CreatePackageCommand commandWithoutNotes = CreatePackageCommand.builder()
                    .recipientName("John Doe")
                    .recipientEmail("john.doe@email.com")
                    .recipientPhone("+1234567890")
                    .street("123 Main St")
                    .city("New York")
                    .state("NY")
                    .country("USA")
                    .postalCode("10001")
                    .height(20.0)
                    .width(15.0)
                    .depth(10.0)
                    .weight(2.5)
                    .notes(null)
                    .build();

            // Act
            Package result = packageFactory.createPackage(commandWithoutNotes);

            // Assert
            assertThat(result.getNotes()).isNull();
        }

        @Test
        @DisplayName("Should create package without state when state is null")
        void shouldCreatePackageWithoutStateWhenStateIsNull() {
            // Arrange
            CreatePackageCommand commandWithoutState = CreatePackageCommand.builder()
                    .recipientName("John Doe")
                    .recipientEmail("john.doe@email.com")
                    .recipientPhone("+1234567890")
                    .street("123 Main St")
                    .city("New York")
                    .state(null)  // No state
                    .country("USA")
                    .postalCode("10001")
                    .height(20.0)
                    .width(15.0)
                    .depth(10.0)
                    .weight(2.5)
                    .notes("Test package")
                    .build();

            // Act
            Package result = packageFactory.createPackage(commandWithoutState);

            // Assert
            assertThat(result.getRecipient().getAddress().getState()).isNull();
        }
    }

    @Nested
    @DisplayName("Invalid Data Tests")
    class InvalidDataTests {

        @Test
        @DisplayName("Should fail when recipient name is null")
        void shouldFailWhenRecipientNameIsNull() {
            // Arrange
            CreatePackageCommand invalidCommand = CreatePackageCommand.builder()
                    .recipientName(null)
                    .recipientEmail("john.doe@email.com")
                    .recipientPhone("+1234567890")
                    .street("123 Main St")
                    .city("New York")
                    .state("NY")
                    .country("USA")
                    .postalCode("10001")
                    .height(20.0)
                    .width(15.0)
                    .depth(10.0)
                    .weight(2.5)
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> packageFactory.createPackage(invalidCommand))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessageContaining("Recipient name is required");
        }

        @Test
        @DisplayName("Should fail when recipient email is invalid")
        void shouldFailWhenRecipientEmailIsInvalid() {
            // Arrange
            CreatePackageCommand invalidCommand = CreatePackageCommand.builder()
                    .recipientName("John Doe")
                    .recipientEmail("invalid-email")
                    .recipientPhone("+1234567890")
                    .street("123 Main St")
                    .city("New York")
                    .state("NY")
                    .country("USA")
                    .postalCode("10001")
                    .height(20.0)
                    .width(15.0)
                    .depth(10.0)
                    .weight(2.5)
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> packageFactory.createPackage(invalidCommand))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessageContaining("Invalid email format");
        }

        @Test
        @DisplayName("Should fail when recipient phone is invalid")
        void shouldFailWhenRecipientPhoneIsInvalid() {
            // Arrange
            CreatePackageCommand invalidCommand = CreatePackageCommand.builder()
                    .recipientName("John Doe")
                    .recipientEmail("john.doe@email.com")
                    .recipientPhone("invalid-phone")
                    .street("123 Main St")
                    .city("New York")
                    .state("NY")
                    .country("USA")
                    .postalCode("10001")
                    .height(20.0)
                    .width(15.0)
                    .depth(10.0)
                    .weight(2.5)
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> packageFactory.createPackage(invalidCommand))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessageContaining("Invalid phone number");
        }

        @Test
        @DisplayName("Should fail when weight is negative")
        void shouldFailWhenWeightIsNegative() {
            // Arrange
            CreatePackageCommand invalidCommand = CreatePackageCommand.builder()
                    .recipientName("John Doe")
                    .recipientEmail("john.doe@email.com")
                    .recipientPhone("+1234567890")
                    .street("123 Main St")
                    .city("New York")
                    .state("NY")
                    .country("USA")
                    .postalCode("10001")
                    .height(20.0)
                    .width(15.0)
                    .depth(10.0)
                    .weight(-1.0)  // Negative weight
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> packageFactory.createPackage(invalidCommand))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessageContaining("Weight must be positive");
        }

        @Test
        @DisplayName("Should fail when weight exceeds maximum")
        void shouldFailWhenWeightExceedsMaximum() {
            // Arrange
            CreatePackageCommand invalidCommand = CreatePackageCommand.builder()
                    .recipientName("John Doe")
                    .recipientEmail("john.doe@email.com")
                    .recipientPhone("+1234567890")
                    .street("123 Main St")
                    .city("New York")
                    .state("NY")
                    .country("USA")
                    .postalCode("10001")
                    .height(20.0)
                    .width(15.0)
                    .depth(10.0)
                    .weight(1001.0)  // Exceeds maximum
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> packageFactory.createPackage(invalidCommand))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessageContaining("Weight cannot exceed 1000 kg");
        }

        @Test
        @DisplayName("Should fail when dimension is negative")
        void shouldFailWhenDimensionIsNegative() {
            // Arrange
            CreatePackageCommand invalidCommand = CreatePackageCommand.builder()
                    .recipientName("John Doe")
                    .recipientEmail("john.doe@email.com")
                    .recipientPhone("+1234567890")
                    .street("123 Main St")
                    .city("New York")
                    .state("NY")
                    .country("USA")
                    .postalCode("10001")
                    .height(-1.0)  // Negative height
                    .width(15.0)
                    .depth(10.0)
                    .weight(2.5)
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> packageFactory.createPackage(invalidCommand))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessageContaining("Height must be positive");
        }

        @Test
        @DisplayName("Should fail when address is incomplete")
        void shouldFailWhenAddressIsIncomplete() {
            // Arrange
            CreatePackageCommand invalidCommand = CreatePackageCommand.builder()
                    .recipientName("John Doe")
                    .recipientEmail("john.doe@email.com")
                    .recipientPhone("+1234567890")
                    .street(null)  // Missing street
                    .city("New York")
                    .state("NY")
                    .country("USA")
                    .postalCode("10001")
                    .height(20.0)
                    .width(15.0)
                    .depth(10.0)
                    .weight(2.5)
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> packageFactory.createPackage(invalidCommand))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessageContaining("Street is required");
        }
    }

    @Nested
    @DisplayName("Boundary Value Tests")
    class BoundaryValueTests {

        @Test
        @DisplayName("Should create package with minimum valid weight")
        void shouldCreatePackageWithMinimumValidWeight() {
            // Arrange
            CreatePackageCommand commandWithMinWeight = CreatePackageCommand.builder()
                    .recipientName("John Doe")
                    .recipientEmail("john.doe@email.com")
                    .recipientPhone("+1234567890")
                    .street("123 Main St")
                    .city("New York")
                    .state("NY")
                    .country("USA")
                    .postalCode("10001")
                    .height(20.0)
                    .width(15.0)
                    .depth(10.0)
                    .weight(0.001)  // Minimum positive weight
                    .build();

            // Act
            Package result = packageFactory.createPackage(commandWithMinWeight);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getWeight().toKilograms()).isEqualTo(0.001);
        }

        @Test
        @DisplayName("Should create package with maximum valid weight")
        void shouldCreatePackageWithMaximumValidWeight() {
            // Arrange
            CreatePackageCommand commandWithMaxWeight = CreatePackageCommand.builder()
                    .recipientName("John Doe")
                    .recipientEmail("john.doe@email.com")
                    .recipientPhone("+1234567890")
                    .street("123 Main St")
                    .city("New York")
                    .state("NY")
                    .country("USA")
                    .postalCode("10001")
                    .height(20.0)
                    .width(15.0)
                    .depth(10.0)
                    .weight(1000.0)  // Maximum weight
                    .build();

            // Act
            Package result = packageFactory.createPackage(commandWithMaxWeight);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getWeight().toKilograms()).isEqualTo(1000.0);
        }

        @Test
        @DisplayName("Should create package with maximum valid dimensions")
        void shouldCreatePackageWithMaximumValidDimensions() {
            // Arrange
            CreatePackageCommand commandWithMaxDimensions = CreatePackageCommand.builder()
                    .recipientName("John Doe")
                    .recipientEmail("john.doe@email.com")
                    .recipientPhone("+1234567890")
                    .street("123 Main St")
                    .city("New York")
                    .state("NY")
                    .country("USA")
                    .postalCode("10001")
                    .height(500.0)  // Maximum dimensions
                    .width(500.0)
                    .depth(500.0)
                    .weight(2.5)
                    .build();

            // Act
            Package result = packageFactory.createPackage(commandWithMaxDimensions);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getDimensions().getHeight().doubleValue()).isEqualTo(500.0);
            assertThat(result.getDimensions().getWidth().doubleValue()).isEqualTo(500.0);
            assertThat(result.getDimensions().getDepth().doubleValue()).isEqualTo(500.0);
        }
    }
}
