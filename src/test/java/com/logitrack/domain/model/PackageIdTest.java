package com.logitrack.domain.model;

import com.logitrack.domain.exception.InvalidPackageDataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PackageId Tests")
class PackageIdTest {

    private static final Pattern VALID_PATTERN = Pattern.compile("^LT-[A-Z0-9]{9}$");

    @Nested
    @DisplayName("Generation Tests")
    class GenerationTests {

        @Test
        @DisplayName("Should generate valid package ID")
        void shouldGenerateValidPackageId() {
            // Arrange & Act
            PackageId packageId = PackageId.generate();

            // Assert
            assertThat(packageId).isNotNull();
            assertThat(packageId.getValue()).isNotNull();
            assertThat(packageId.getValue()).startsWith("LT-");
            assertThat(packageId.getValue()).hasSize(12); // "LT-" + 9 characters
            assertThat(VALID_PATTERN.matcher(packageId.getValue()).matches()).isTrue();
        }

        @Test
        @DisplayName("Should generate different IDs on multiple calls")
        void shouldGenerateDifferentIDsOnMultipleCalls() {
            // Arrange & Act
            PackageId id1 = PackageId.generate();
            PackageId id2 = PackageId.generate();
            PackageId id3 = PackageId.generate();

            // Assert
            assertThat(id1.getValue()).isNotEqualTo(id2.getValue());
            assertThat(id2.getValue()).isNotEqualTo(id3.getValue());
            assertThat(id1.getValue()).isNotEqualTo(id3.getValue());
        }

        @Test
        @DisplayName("Should generate IDs with uppercase letters and numbers only")
        void shouldGenerateIDsWithUppercaseLettersAndNumbersOnly() {
            // Arrange & Act
            PackageId packageId = PackageId.generate();
            String idPart = packageId.getValue().substring(3); // Remove "LT-" prefix

            // Assert
            assertThat(idPart).matches("[A-Z0-9]{9}");
            assertThat(idPart).doesNotContain("-");
            assertThat(idPart).doesNotMatch(".*[a-z].*"); // No lowercase letters
        }

        @Test
        @DisplayName("Should generate unique IDs in bulk")
        void shouldGenerateUniqueIDsInBulk() {
            // Arrange
            Set<String> generatedIds = new HashSet<>();
            int numberOfIds = 1000;

            // Act
            for (int i = 0; i < numberOfIds; i++) {
                PackageId packageId = PackageId.generate();
                generatedIds.add(packageId.getValue());
            }

            // Assert
            assertThat(generatedIds).hasSize(numberOfIds); // All should be unique
        }

        @Test
        @DisplayName("Should generate ID with correct format structure")
        void shouldGenerateIdWithCorrectFormatStructure() {
            // Arrange & Act
            PackageId packageId = PackageId.generate();

            // Assert
            String value = packageId.getValue();
            assertThat(value).hasSize(12);
            assertThat(value.substring(0, 3)).isEqualTo("LT-");
            assertThat(value.substring(3)).hasSize(9);
            assertThat(VALID_PATTERN.matcher(value).matches()).isTrue();
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Should create package ID from valid string")
        void shouldCreatePackageIdFromValidString() {
            // Arrange
            String validId = "LT-ABC123DEF";

            // Act
            PackageId packageId = PackageId.of(validId);

            // Assert
            assertThat(packageId.getValue()).isEqualTo(validId);
        }

        @Test
        @DisplayName("Should create package ID with numbers only")
        void shouldCreatePackageIdWithNumbersOnly() {
            // Arrange
            String validId = "LT-123456789";

            // Act
            PackageId packageId = PackageId.of(validId);

            // Assert
            assertThat(packageId.getValue()).isEqualTo(validId);
        }

        @Test
        @DisplayName("Should create package ID with letters only")
        void shouldCreatePackageIdWithLettersOnly() {
            // Arrange
            String validId = "LT-ABCDEFGHI";

            // Act
            PackageId packageId = PackageId.of(validId);

            // Assert
            assertThat(packageId.getValue()).isEqualTo(validId);
        }

        @Test
        @DisplayName("Should throw exception for null value")
        void shouldThrowExceptionForNullValue() {
            // Arrange
            String nullValue = null;

            // Act & Assert
            assertThatThrownBy(() -> PackageId.of(nullValue))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Invalid package ID format: null");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "ABC123DEF",        // Missing prefix
                "LT-",              // Missing ID part
                "LT-ABC123DE",      // Too short (8 chars)
                "LT-ABC123DEFG",    // Too long (10 chars)
                "LT-abc123def",     // Lowercase letters
                "LT-ABC-123DE",     // Contains dash in ID part
                "LT-ABC 123DE",     // Contains space
                "LT-ABC123DE!",     // Contains special character
                "",                 // Empty string
                "   ",              // Whitespace only
                "XY-ABC123DEF",     // Wrong prefix
                "lt-ABC123DEF",     // Lowercase prefix
                "LT_ABC123DEF"      // Underscore instead of dash
        })
        @DisplayName("Should throw exception for invalid formats")
        void shouldThrowExceptionForInvalidFormats(String invalidId) {
            // Arrange - invalidId from parameter

            // Act & Assert
            assertThatThrownBy(() -> PackageId.of(invalidId))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Invalid package ID format: " + invalidId);
        }
    }

    @Nested
    @DisplayName("Value Object Tests")
    class ValueObjectTests {

        @Test
        @DisplayName("Should be equal when values are same")
        void shouldBeEqualWhenValuesAreSame() {
            // Arrange
            String idValue = "LT-ABC123DEF";
            PackageId id1 = PackageId.of(idValue);
            PackageId id2 = PackageId.of(idValue);

            // Act & Assert
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when values are different")
        void shouldNotBeEqualWhenValuesAreDifferent() {
            // Arrange
            PackageId id1 = PackageId.of("LT-ABC123DEF");
            PackageId id2 = PackageId.of("LT-XYZ789GHI");

            // Act & Assert
            assertThat(id1).isNotEqualTo(id2);
            assertThat(id1.hashCode()).isNotEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("Should be immutable")
        void shouldBeImmutable() {
            // Arrange
            String originalValue = "LT-ABC123DEF";
            PackageId packageId = PackageId.of(originalValue);

            // Act & Assert
            assertThat(packageId.getValue()).isEqualTo(originalValue);
            // Cannot modify value as it's a @Value class (immutable)
            // This test verifies the immutable nature through the @Value annotation
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return value in toString")
        void shouldReturnValueInToString() {
            // Arrange
            String idValue = "LT-ABC123DEF";
            PackageId packageId = PackageId.of(idValue);

            // Act
            String result = packageId.toString();

            // Assert
            assertThat(result).isEqualTo(idValue);
        }

        @Test
        @DisplayName("Should have consistent toString for generated IDs")
        void shouldHaveConsistentToStringForGeneratedIDs() {
            // Arrange
            PackageId packageId = PackageId.generate();

            // Act
            String toString1 = packageId.toString();
            String toString2 = packageId.toString();

            // Assert
            assertThat(toString1).isEqualTo(toString2);
            assertThat(toString1).isEqualTo(packageId.getValue());
        }
    }

    @Nested
    @DisplayName("Pattern Validation Tests")
    class PatternValidationTests {

        @Test
        @DisplayName("Should accept valid boundary cases")
        void shouldAcceptValidBoundaryCases() {
            // Arrange & Act & Assert
            assertThatCode(() -> PackageId.of("LT-000000000")).doesNotThrowAnyException();
            assertThatCode(() -> PackageId.of("LT-ZZZZZZZZZ")).doesNotThrowAnyException();
            assertThatCode(() -> PackageId.of("LT-999999999")).doesNotThrowAnyException();
            assertThatCode(() -> PackageId.of("LT-AAAAAAAAA")).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should reject invalid character combinations")
        void shouldRejectInvalidCharacterCombinations() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> PackageId.of("LT-ABC123DE@"))
                    .isInstanceOf(InvalidPackageDataException.class);

            assertThatThrownBy(() -> PackageId.of("LT-ABC123DE#"))
                    .isInstanceOf(InvalidPackageDataException.class);

            assertThatThrownBy(() -> PackageId.of("LT-ABC123DE$"))
                    .isInstanceOf(InvalidPackageDataException.class);
        }

        @Test
        @DisplayName("Should validate exact length requirement")
        void shouldValidateExactLengthRequirement() {
            // Arrange & Act & Assert
            // Exactly 9 characters after prefix should work
            assertThatCode(() -> PackageId.of("LT-ABCDEF123")).doesNotThrowAnyException();

            // Less than 9 characters should fail
            assertThatThrownBy(() -> PackageId.of("LT-ABCDEF12"))
                    .isInstanceOf(InvalidPackageDataException.class);

            // More than 9 characters should fail
            assertThatThrownBy(() -> PackageId.of("LT-ABCDEF1234"))
                    .isInstanceOf(InvalidPackageDataException.class);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle mixed alphanumeric combinations")
        void shouldHandleMixedAlphanumericCombinations() {
            // Arrange
            String[] validMixedIds = {
                    "LT-A1B2C3D4E",
                    "LT-123ABC456",
                    "LT-9Z8Y7X6W5",
                    "LT-ABCD12345"
            };

            // Act & Assert
            for (String validId : validMixedIds) {
                assertThatCode(() -> PackageId.of(validId))
                        .doesNotThrowAnyException();

                PackageId packageId = PackageId.of(validId);
                assertThat(packageId.getValue()).isEqualTo(validId);
            }
        }

        @Test
        @DisplayName("Should maintain case sensitivity")
        void shouldMaintainCaseSensitivity() {
            // Arrange
            String uppercaseId = "LT-ABCDEF123";
            String lowercaseId = "LT-abcdef123";

            // Act & Assert
            assertThatCode(() -> PackageId.of(uppercaseId)).doesNotThrowAnyException();

            assertThatThrownBy(() -> PackageId.of(lowercaseId))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Invalid package ID format: " + lowercaseId);
        }

        @Test
        @DisplayName("Should handle Unicode characters correctly")
        void shouldHandleUnicodeCharactersCorrectly() {
            // Arrange
            String unicodeId = "LT-ABC123DÉF";

            // Act & Assert
            assertThatThrownBy(() -> PackageId.of(unicodeId))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Invalid package ID format: " + unicodeId);
        }
    }
}
