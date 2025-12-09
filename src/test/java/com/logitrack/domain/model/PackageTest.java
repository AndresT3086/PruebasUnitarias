package com.logitrack.domain.model;

import com.logitrack.domain.exception.InvalidPackageDataException;
import com.logitrack.domain.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PackageTest {


    private Package.Builder validPackageBuilder;
    private Recipient validRecipient;
    private Dimensions validDimensions;
    private Weight validWeight;

    @BeforeEach
    void setUp() {
        Recipient.Address address = Recipient.Address.builder()
                .street("123 Main St")
                .city("New York")
                .state("NY")
                .country("USA")
                .postalCode("10001")
                .build();

        validRecipient = Recipient.builder()
                .name("John Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .address(address)
                .build();

        validDimensions = Dimensions.of(20, 15, 10);
        validWeight = Weight.ofKilograms(2.5);

        validPackageBuilder = Package.builder()
                .recipient(validRecipient)
                .dimensions(validDimensions)
                .weight(validWeight);
    }

    @Nested
    @DisplayName("Package Creation Tests")
    class PackageCreationTests {

        @Test
        @DisplayName("Should create package with valid data")
        void shouldCreatePackageWithValidData() {

            // Act
            Package pkg = validPackageBuilder.build();

            // Assert
            assertThat(pkg).isNotNull();
            assertThat(pkg.getId()).isNotNull();
            assertThat(pkg.getId().getValue()).startsWith("LT-");
            assertThat(pkg.getRecipient()).isEqualTo(validRecipient);
            assertThat(pkg.getDimensions()).isEqualTo(validDimensions);
            assertThat(pkg.getWeight()).isEqualTo(validWeight);
            assertThat(pkg.getStatus()).isEqualTo(PackageStatus.CREATED);
            assertThat(pkg.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("Should fail when recipient is null")
        void shouldFailWhenRecipientIsNull() {
            // Arrange
            Package.Builder builder = Package.builder()
                    .dimensions(validDimensions)
                    .weight(validWeight);

            // Act & Assert
            assertThatThrownBy(() -> builder.build())
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessageContaining("Recipient cannot be null");
        }

        @Test
        @DisplayName("Should fail when dimensions are null")
        void shouldFailWhenDimensionsAreNull() {
            // Arrange
            Package.Builder builder = Package.builder()
                    .recipient(validRecipient)
                    .weight(validWeight);

            // Act & Assert
            assertThatThrownBy(() -> builder.build())
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessageContaining("Dimensions cannot be null");
        }
    }

    @Nested
    @DisplayName("State Transition Tests")
    class StateTransitionTests {

        private Package pkg;

        @BeforeEach
        void setUp() {
            pkg = validPackageBuilder.build();
        }

        @Test
        @DisplayName("Should transition from CREATED to IN_TRANSIT")
        void shouldTransitionFromCreatedToInTransit() {
            // Arrange
            assertThat(pkg.getStatus()).isEqualTo(PackageStatus.CREATED);

            // Act
            pkg.changeStatus(PackageStatus.IN_TRANSIT);

            // Assert
            assertThat(pkg.getStatus()).isEqualTo(PackageStatus.IN_TRANSIT);
            assertThat(pkg.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should transition from IN_TRANSIT to OUT_FOR_DELIVERY")
        void shouldTransitionFromInTransitToOutForDelivery() {
            // Arrange
            pkg.changeStatus(PackageStatus.IN_TRANSIT);

            // Act
            pkg.changeStatus(PackageStatus.OUT_FOR_DELIVERY);

            // Assert
            assertThat(pkg.getStatus()).isEqualTo(PackageStatus.OUT_FOR_DELIVERY);
        }

        @Test
        @DisplayName("Should transition from OUT_FOR_DELIVERY to DELIVERED")
        void shouldTransitionFromOutForDeliveryToDelivered() {
            // Arrange
            pkg.changeStatus(PackageStatus.IN_TRANSIT);
            pkg.changeStatus(PackageStatus.OUT_FOR_DELIVERY);

            // Act
            pkg.changeStatus(PackageStatus.DELIVERED);

            // Assert
            assertThat(pkg.getStatus()).isEqualTo(PackageStatus.DELIVERED);
            assertThat(pkg.isDelivered()).isTrue();
        }

        @Test
        @DisplayName("Should not allow invalid transition from CREATED to DELIVERED")
        void shouldNotAllowInvalidTransition() {
            // Arrange
            assertThat(pkg.getStatus()).isEqualTo(PackageStatus.CREATED);

            // Act & Assert
            assertThatThrownBy(() -> pkg.changeStatus(PackageStatus.DELIVERED))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessageContaining("Cannot transition from CREATED to DELIVERED");
        }

        @Test
        @DisplayName("Should transition from DELIVERY_FAILED back to IN_TRANSIT")
        void shouldTransitionFromDeliveryFailedToInTransit() {
            // Arrange
            pkg.changeStatus(PackageStatus.IN_TRANSIT);
            pkg.changeStatus(PackageStatus.OUT_FOR_DELIVERY);
            pkg.changeStatus(PackageStatus.DELIVERY_FAILED);

            // Act
            pkg.changeStatus(PackageStatus.IN_TRANSIT);

            // Assert
            assertThat(pkg.getStatus()).isEqualTo(PackageStatus.IN_TRANSIT);
        }
    }

    @Nested
    @DisplayName("Location Management Tests")
    class LocationManagementTests {

        private Package pkg;

        @BeforeEach
        void setUp() {
            pkg = validPackageBuilder.build();
        }

        @Test
        @DisplayName("Should add location to package")
        void shouldAddLocationToPackage() {
            // Arrange
            Location location = Location.create(
                    "Los Angeles", "USA", "In transit", 34.0522, -118.2437
            );

            // Act
            pkg.addLocation(location);

            // Assert
            assertThat(pkg.getCurrentLocation()).isPresent();
            assertThat(pkg.getCurrentLocation().get()).isEqualTo(location);
            assertThat(pkg.getLocationHistory().size()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should fail when adding null location")
        void shouldFailWhenAddingNullLocation() {
            // Arrange
            Location nullLocation = null;

            // Act & Assert
            assertThatThrownBy(() -> pkg.addLocation(nullLocation))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessageContaining("Location cannot be null");
        }
    }
}
