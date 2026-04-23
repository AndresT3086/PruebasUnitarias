package com.logitrack.domain.model;

import com.logitrack.domain.exception.InvalidPackageDataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Recipient Tests")
class RecipientTest {

    @Nested
    @DisplayName("Recipient Constructor Tests")
    class RecipientConstructorTests {

        @Test
        @DisplayName("Should create recipient with valid data")
        void shouldCreateRecipientWithValidData() {
            // Arrange
            String name = "John Doe";
            String email = "john.doe@example.com";
            String phone = "+1-234-567-8900";
            Recipient.Address address = Recipient.Address.builder()
                    .street("123 Main St")
                    .city("New York")
                    .state("NY")
                    .country("USA")
                    .postalCode("10001")
                    .build();

            // Act
            Recipient recipient = new Recipient(name, email, phone, address);

            // Assert
            assertThat(recipient.getName()).isEqualTo("John Doe");
            assertThat(recipient.getEmail()).isEqualTo("john.doe@example.com");
            assertThat(recipient.getPhone()).isEqualTo("+12345678900");
            assertThat(recipient.getAddress()).isEqualTo(address);
        }

        @Test
        @DisplayName("Should create recipient using builder")
        void shouldCreateRecipientUsingBuilder() {
            // Arrange
            Recipient.Address address = Recipient.Address.builder()
                    .street("456 Oak Ave")
                    .city("Los Angeles")
                    .state("CA")
                    .country("USA")
                    .postalCode("90210")
                    .build();

            // Act
            Recipient recipient = Recipient.builder()
                    .name("Jane Smith")
                    .email("jane.smith@test.com")
                    .phone("+1234567890")
                    .address(address)
                    .build();

            // Assert
            assertThat(recipient.getName()).isEqualTo("Jane Smith");
            assertThat(recipient.getEmail()).isEqualTo("jane.smith@test.com");
            assertThat(recipient.getPhone()).isEqualTo("+1234567890");
            assertThat(recipient.getAddress()).isEqualTo(address);
        }
    }

    @Nested
    @DisplayName("Name Validation Tests")
    class NameValidationTests {

        @Test
        @DisplayName("Should trim whitespace from name")
        void shouldTrimWhitespaceFromName() {
            // Arrange
            String nameWithSpaces = "  John Doe  ";
            Recipient.Address address = createValidAddress();

            // Act
            Recipient recipient = new Recipient(nameWithSpaces, "john@test.com", "+1234567890", address);

            // Assert
            assertThat(recipient.getName()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("Should throw exception for null name")
        void shouldThrowExceptionForNullName() {
            // Arrange
            String name = null;
            Recipient.Address address = createValidAddress();

            // Act & Assert
            assertThatThrownBy(() -> new Recipient(name, "john@test.com", "+1234567890", address))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Recipient name is required");
        }

        @Test
        @DisplayName("Should throw exception for empty name")
        void shouldThrowExceptionForEmptyName() {
            // Arrange
            String name = "";
            Recipient.Address address = createValidAddress();

            // Act & Assert
            assertThatThrownBy(() -> new Recipient(name, "john@test.com", "+1234567890", address))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Recipient name is required");
        }

        @Test
        @DisplayName("Should throw exception for whitespace-only name")
        void shouldThrowExceptionForWhitespaceOnlyName() {
            // Arrange
            String name = "   ";
            Recipient.Address address = createValidAddress();

            // Act & Assert
            assertThatThrownBy(() -> new Recipient(name, "john@test.com", "+1234567890", address))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Recipient name is required");
        }

        @Test
        @DisplayName("Should throw exception for name too long")
        void shouldThrowExceptionForNameTooLong() {
            // Arrange
            String longName = "A".repeat(101); // 101 characters
            Recipient.Address address = createValidAddress();

            // Act & Assert
            assertThatThrownBy(() -> new Recipient(longName, "john@test.com", "+1234567890", address))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Recipient name too long");
        }

        @Test
        @DisplayName("Should accept name at maximum length")
        void shouldAcceptNameAtMaximumLength() {
            // Arrange
            String maxLengthName = "A".repeat(100); // Exactly 100 characters
            Recipient.Address address = createValidAddress();

            // Act
            Recipient recipient = new Recipient(maxLengthName, "john@test.com", "+1234567890", address);

            // Assert
            assertThat(recipient.getName()).isEqualTo(maxLengthName);
        }
    }

    @Nested
    @DisplayName("Email Validation Tests")
    class EmailValidationTests {

        @Test
        @DisplayName("Should convert email to lowercase")
        void shouldConvertEmailToLowercase() {
            // Arrange
            String uppercaseEmail = "JOHN.DOE@EXAMPLE.COM";
            Recipient.Address address = createValidAddress();

            // Act
            Recipient recipient = new Recipient("John Doe", uppercaseEmail, "+1234567890", address);

            // Assert
            assertThat(recipient.getEmail()).isEqualTo("john.doe@example.com");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "user@example.com",
                "test.email@domain.co.uk",
                "user+tag@example.org",
                "user_name@example-domain.com",
                "123@example.com",
                "a@b.co"
        })
        @DisplayName("Should accept valid email formats")
        void shouldAcceptValidEmailFormats(String email) {
            // Arrange
            Recipient.Address address = createValidAddress();

            // Act & Assert
            assertThatCode(() -> new Recipient("John Doe", email, "+1234567890", address))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should throw exception for null email")
        void shouldThrowExceptionForNullEmail() {
            // Arrange
            String email = null;
            Recipient.Address address = createValidAddress();

            // Act & Assert
            assertThatThrownBy(() -> new Recipient("John Doe", email, "+1234567890", address))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Invalid email format");
        }
    }

    @Nested
    @DisplayName("Phone Validation Tests")
    class PhoneValidationTests {

        @Test
        @DisplayName("Should clean phone number by removing spaces and dashes")
        void shouldCleanPhoneNumberByRemovingSpacesAndDashes() {
            // Arrange
            String phoneWithSpaces = "+1 234 567 8900";
            String phoneWithDashes = "+1-234-567-8900";
            String phoneWithBoth = "+1 234-567 8900";
            Recipient.Address address = createValidAddress();

            // Act
            Recipient recipient1 = new Recipient("John", "john@test.com", phoneWithSpaces, address);
            Recipient recipient2 = new Recipient("Jane", "jane@test.com", phoneWithDashes, address);
            Recipient recipient3 = new Recipient("Bob", "bob@test.com", phoneWithBoth, address);

            // Assert
            assertThat(recipient1.getPhone()).isEqualTo("+12345678900");
            assertThat(recipient2.getPhone()).isEqualTo("+12345678900");
            assertThat(recipient3.getPhone()).isEqualTo("+12345678900");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "+1234567890",
                "+12345678901234",
                "1234567890",
                "+44123456789",
                "+81234567890"
        })
        @DisplayName("Should accept valid phone formats")
        void shouldAcceptValidPhoneFormats(String phone) {
            // Arrange
            Recipient.Address address = createValidAddress();

            // Act & Assert
            assertThatCode(() -> new Recipient("John Doe", "john@test.com", phone, address))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should throw exception for null phone")
        void shouldThrowExceptionForNullPhone() {
            // Arrange
            String phone = null;
            Recipient.Address address = createValidAddress();

            // Act & Assert
            assertThatThrownBy(() -> new Recipient("John Doe", "john@test.com", phone, address))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Invalid phone number");
        }
    }

    @Nested
    @DisplayName("Address Validation Tests")
    class AddressValidationTests {

        @Test
        @DisplayName("Should throw exception for null address")
        void shouldThrowExceptionForNullAddress() {
            // Arrange
            Recipient.Address address = null;

            // Act & Assert
            assertThatThrownBy(() -> new Recipient("John Doe", "john@test.com", "+1234567890", address))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Address is required");
        }
    }

    @Nested
    @DisplayName("Address Class Tests")
    class AddressClassTests {

        @Test
        @DisplayName("Should create address with all fields")
        void shouldCreateAddressWithAllFields() {
            // Arrange & Act
            Recipient.Address address = new Recipient.Address(
                    "123 Main St", "New York", "NY", "USA", "10001"
            );

            // Assert
            assertThat(address.getStreet()).isEqualTo("123 Main St");
            assertThat(address.getCity()).isEqualTo("New York");
            assertThat(address.getState()).isEqualTo("NY");
            assertThat(address.getCountry()).isEqualTo("USA");
            assertThat(address.getPostalCode()).isEqualTo("10001");
        }

        @Test
        @DisplayName("Should create address without state")
        void shouldCreateAddressWithoutState() {
            // Arrange & Act
            Recipient.Address address = new Recipient.Address(
                    "456 Oak Ave", "London", null, "UK", "SW1A 1AA"
            );

            // Assert
            assertThat(address.getStreet()).isEqualTo("456 Oak Ave");
            assertThat(address.getCity()).isEqualTo("London");
            assertThat(address.getState()).isNull();
            assertThat(address.getCountry()).isEqualTo("UK");
            assertThat(address.getPostalCode()).isEqualTo("SW1A1AA");
        }

        @Test
        @DisplayName("Should trim whitespace from address fields")
        void shouldTrimWhitespaceFromAddressFields() {
            // Arrange & Act
            Recipient.Address address = new Recipient.Address(
                    "  123 Main St  ", "  New York  ", " NY ", "  USA  ", " 10001 "
            );

            // Assert
            assertThat(address.getStreet()).isEqualTo("123 Main St");
            assertThat(address.getCity()).isEqualTo("New York");
            assertThat(address.getState()).isEqualTo(" NY ");
            assertThat(address.getCountry()).isEqualTo("USA");
            assertThat(address.getPostalCode()).isEqualTo("10001");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Should throw exception for empty required address fields")
        void shouldThrowExceptionForEmptyRequiredAddressFields(String emptyValue) {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new Recipient.Address(emptyValue, "City", "State", "Country", "12345"))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Street is required");

            assertThatThrownBy(() -> new Recipient.Address("Street", emptyValue, "State", "Country", "12345"))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("City is required");

            assertThatThrownBy(() -> new Recipient.Address("Street", "City", "State", emptyValue, "12345"))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Country is required");

            assertThatThrownBy(() -> new Recipient.Address("Street", "City", "State", "Country", emptyValue))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Postal code is required");
        }

        @Test
        @DisplayName("Should throw exception for null required address fields")
        void shouldThrowExceptionForNullRequiredAddressFields() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new Recipient.Address(null, "City", "State", "Country", "12345"))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Street is required");

            assertThatThrownBy(() -> new Recipient.Address("Street", null, "State", "Country", "12345"))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("City is required");

            assertThatThrownBy(() -> new Recipient.Address("Street", "City", "State", null, "12345"))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Country is required");

            assertThatThrownBy(() -> new Recipient.Address("Street", "City", "State", "Country", null))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Postal code is required");
        }
    }

    @Nested
    @DisplayName("Postal Code Validation Tests")
    class PostalCodeValidationTests {

        @Test
        @DisplayName("Should clean postal code by removing spaces and dashes")
        void shouldCleanPostalCodeByRemovingSpacesAndDashes() {
            // Arrange & Act
            Recipient.Address address = new Recipient.Address(
                    "123 Main St", "City", "State", "Country", "SW1A 1AA"
            );

            // Assert
            assertThat(address.getPostalCode()).isEqualTo("SW1A1AA");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "12345",
                "SW1A1AA",
                "K1A-0A6",
                "123",
                "1234567890"
        })
        @DisplayName("Should accept valid postal code lengths")
        void shouldAcceptValidPostalCodeLengths(String postalCode) {
            // Arrange & Act & Assert
            assertThatCode(() -> new Recipient.Address("Street", "City", "State", "Country", postalCode))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "12",           // Too short
                "12345678901"   // Too long
        })
        @DisplayName("Should reject invalid postal code lengths")
        void shouldRejectInvalidPostalCodeLengths(String postalCode) {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new Recipient.Address("Street", "City", "State", "Country", postalCode))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Invalid postal code");
        }
    }

    @Nested
    @DisplayName("Full Address Format Tests")
    class FullAddressFormatTests {

        @Test
        @DisplayName("Should format full address with state")
        void shouldFormatFullAddressWithState() {
            // Arrange
            Recipient.Address address = new Recipient.Address(
                    "123 Main St", "New York", "NY", "USA", "10001"
            );

            // Act
            String fullAddress = address.getFullAddress();

            // Assert
            assertThat(fullAddress).isEqualTo("123 Main St, New York, NY, USA 10001");
        }

        @Test
        @DisplayName("Should format full address without state")
        void shouldFormatFullAddressWithoutState() {
            // Arrange
            Recipient.Address address = new Recipient.Address(
                    "456 Oak Ave", "London", null, "UK", "SW1A1AA"
            );

            // Act
            String fullAddress = address.getFullAddress();

            // Assert
            assertThat(fullAddress).isEqualTo("456 Oak Ave, London, UK SW1A1AA");
        }

        @Test
        @DisplayName("Should format full address with empty state")
        void shouldFormatFullAddressWithEmptyState() {
            // Arrange
            Recipient.Address address = Recipient.Address.builder()
                    .street("789 Pine St")
                    .city("Toronto")
                    .state("")
                    .country("Canada")
                    .postalCode("M5V3A8")
                    .build();

            // Act
            String fullAddress = address.getFullAddress();

            // Assert
            assertThat(fullAddress).isEqualTo("789 Pine St, Toronto, Canada M5V3A8");
        }
    }

    @Nested
    @DisplayName("Value Object Tests")
    class ValueObjectTests {

        @Test
        @DisplayName("Should be equal when all fields are same")
        void shouldBeEqualWhenAllFieldsAreSame() {
            // Arrange
            Recipient.Address address = createValidAddress();
            Recipient recipient1 = new Recipient("John Doe", "john@test.com", "+1234567890", address);
            Recipient recipient2 = new Recipient("John Doe", "john@test.com", "+1234567890", address);

            // Act & Assert
            assertThat(recipient1).isEqualTo(recipient2);
            assertThat(recipient1.hashCode()).isEqualTo(recipient2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when fields differ")
        void shouldNotBeEqualWhenFieldsDiffer() {
            // Arrange
            Recipient.Address address = createValidAddress();
            Recipient recipient1 = new Recipient("John Doe", "john@test.com", "+1234567890", address);
            Recipient recipient2 = new Recipient("Jane Smith", "john@test.com", "+1234567890", address);

            // Act & Assert
            assertThat(recipient1).isNotEqualTo(recipient2);
        }
    }

    // Helper method
    private Recipient.Address createValidAddress() {
        return Recipient.Address.builder()
                .street("123 Test St")
                .city("Test City")
                .state("TS")
                .country("Test Country")
                .postalCode("12345")
                .build();
    }
}
