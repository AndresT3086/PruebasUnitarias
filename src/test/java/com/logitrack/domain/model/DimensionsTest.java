package com.logitrack.domain.model;

import com.logitrack.domain.exception.InvalidPackageDataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Dimensions Tests")
class DimensionsTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create dimensions with valid values")
        void shouldCreateDimensionsWithValidValues() {
            // Arrange
            BigDecimal height = new BigDecimal("20.5");
            BigDecimal width = new BigDecimal("15.75");
            BigDecimal depth = new BigDecimal("10.25");

            // Act
            Dimensions dimensions = new Dimensions(height, width, depth);

            // Assert
            assertThat(dimensions.getHeight()).isEqualByComparingTo("20.50");
            assertThat(dimensions.getWidth()).isEqualByComparingTo("15.75");
            assertThat(dimensions.getDepth()).isEqualByComparingTo("10.25");
        }

        @Test
        @DisplayName("Should round values to 2 decimal places")
        void shouldRoundValuesTo2DecimalPlaces() {
            // Arrange
            BigDecimal height = new BigDecimal("20.555");
            BigDecimal width = new BigDecimal("15.754");
            BigDecimal depth = new BigDecimal("10.251");

            // Act
            Dimensions dimensions = new Dimensions(height, width, depth);

            // Assert
            assertThat(dimensions.getHeight()).isEqualByComparingTo("20.56"); // Rounded up
            assertThat(dimensions.getWidth()).isEqualByComparingTo("15.75"); // Rounded down
            assertThat(dimensions.getDepth()).isEqualByComparingTo("10.25"); // Rounded down
        }

        @Test
        @DisplayName("Should handle exact half values with HALF_UP rounding")
        void shouldHandleExactHalfValuesWithHalfUpRounding() {
            // Arrange
            BigDecimal height = new BigDecimal("20.125");
            BigDecimal width = new BigDecimal("15.135");
            BigDecimal depth = new BigDecimal("10.145");

            // Act
            Dimensions dimensions = new Dimensions(height, width, depth);

            // Assert
            assertThat(dimensions.getHeight()).isEqualByComparingTo("20.13"); // .125 -> .13 (up)
            assertThat(dimensions.getWidth()).isEqualByComparingTo("15.14"); // .135 -> .14 (up)
            assertThat(dimensions.getDepth()).isEqualByComparingTo("10.15"); // .145 -> .15 (up)
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when height is null")
        void shouldThrowExceptionWhenHeightIsNull() {
            // Arrange
            BigDecimal height = null;
            BigDecimal width = new BigDecimal("15");
            BigDecimal depth = new BigDecimal("10");

            // Act & Assert
            assertThatThrownBy(() -> new Dimensions(height, width, depth))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Height must be positive");
        }

        @Test
        @DisplayName("Should throw exception when width is null")
        void shouldThrowExceptionWhenWidthIsNull() {
            // Arrange
            BigDecimal height = new BigDecimal("20");
            BigDecimal width = null;
            BigDecimal depth = new BigDecimal("10");

            // Act & Assert
            assertThatThrownBy(() -> new Dimensions(height, width, depth))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Width must be positive");
        }

        @Test
        @DisplayName("Should throw exception when depth is null")
        void shouldThrowExceptionWhenDepthIsNull() {
            // Arrange
            BigDecimal height = new BigDecimal("20");
            BigDecimal width = new BigDecimal("15");
            BigDecimal depth = null;

            // Act & Assert
            assertThatThrownBy(() -> new Dimensions(height, width, depth))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Depth must be positive");
        }

        @Test
        @DisplayName("Should throw exception when height is zero")
        void shouldThrowExceptionWhenHeightIsZero() {
            // Arrange
            BigDecimal height = BigDecimal.ZERO;
            BigDecimal width = new BigDecimal("15");
            BigDecimal depth = new BigDecimal("10");

            // Act & Assert
            assertThatThrownBy(() -> new Dimensions(height, width, depth))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Height must be positive");
        }

        @Test
        @DisplayName("Should throw exception when width is negative")
        void shouldThrowExceptionWhenWidthIsNegative() {
            // Arrange
            BigDecimal height = new BigDecimal("20");
            BigDecimal width = new BigDecimal("-15");
            BigDecimal depth = new BigDecimal("10");

            // Act & Assert
            assertThatThrownBy(() -> new Dimensions(height, width, depth))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Width must be positive");
        }

        @Test
        @DisplayName("Should throw exception when depth exceeds maximum")
        void shouldThrowExceptionWhenDepthExceedsMaximum() {
            // Arrange
            BigDecimal height = new BigDecimal("20");
            BigDecimal width = new BigDecimal("15");
            BigDecimal depth = new BigDecimal("501"); // Exceeds 500 cm limit

            // Act & Assert
            assertThatThrownBy(() -> new Dimensions(height, width, depth))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Depth cannot exceed 500 cm");
        }

        @Test
        @DisplayName("Should throw exception when height exceeds maximum")
        void shouldThrowExceptionWhenHeightExceedsMaximum() {
            // Arrange
            BigDecimal height = new BigDecimal("500.01"); // Just over the limit
            BigDecimal width = new BigDecimal("15");
            BigDecimal depth = new BigDecimal("10");

            // Act & Assert
            assertThatThrownBy(() -> new Dimensions(height, width, depth))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Height cannot exceed 500 cm");
        }

        @Test
        @DisplayName("Should throw exception when width exceeds maximum")
        void shouldThrowExceptionWhenWidthExceedsMaximum() {
            // Arrange
            BigDecimal height = new BigDecimal("20");
            BigDecimal width = new BigDecimal("1000"); // Way over the limit
            BigDecimal depth = new BigDecimal("10");

            // Act & Assert
            assertThatThrownBy(() -> new Dimensions(height, width, depth))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Width cannot exceed 500 cm");
        }
    }

    @Nested
    @DisplayName("Boundary Value Tests")
    class BoundaryValueTests {

        @Test
        @DisplayName("Should accept minimum positive values")
        void shouldAcceptMinimumPositiveValues() {
            // Arrange
            BigDecimal height = new BigDecimal("0.01");
            BigDecimal width = new BigDecimal("0.01");
            BigDecimal depth = new BigDecimal("0.01");

            // Act
            Dimensions dimensions = new Dimensions(height, width, depth);

            // Assert
            assertThat(dimensions.getHeight()).isEqualByComparingTo("0.01");
            assertThat(dimensions.getWidth()).isEqualByComparingTo("0.01");
            assertThat(dimensions.getDepth()).isEqualByComparingTo("0.01");
        }

        @Test
        @DisplayName("Should accept maximum allowed values")
        void shouldAcceptMaximumAllowedValues() {
            // Arrange
            BigDecimal height = new BigDecimal("500");
            BigDecimal width = new BigDecimal("500.00");
            BigDecimal depth = new BigDecimal("499.99");

            // Act
            Dimensions dimensions = new Dimensions(height, width, depth);

            // Assert
            assertThat(dimensions.getHeight()).isEqualByComparingTo("500.00");
            assertThat(dimensions.getWidth()).isEqualByComparingTo("500.00");
            assertThat(dimensions.getDepth()).isEqualByComparingTo("499.99");
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "-0.01", "-1", "-100"})
        @DisplayName("Should reject non-positive values")
        void shouldRejectNonPositiveValues(String value) {
            // Arrange
            BigDecimal invalidValue = new BigDecimal(value);
            BigDecimal validValue = new BigDecimal("10");

            // Act & Assert
            assertThatThrownBy(() -> new Dimensions(invalidValue, validValue, validValue))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessageContaining("must be positive");

            assertThatThrownBy(() -> new Dimensions(validValue, invalidValue, validValue))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessageContaining("must be positive");

            assertThatThrownBy(() -> new Dimensions(validValue, validValue, invalidValue))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessageContaining("must be positive");
        }

        @ParameterizedTest
        @ValueSource(strings = {"500.01", "501", "1000", "999999"})
        @DisplayName("Should reject values exceeding maximum")
        void shouldRejectValuesExceedingMaximum(String value) {
            // Arrange
            BigDecimal invalidValue = new BigDecimal(value);
            BigDecimal validValue = new BigDecimal("10");

            // Act & Assert
            assertThatThrownBy(() -> new Dimensions(invalidValue, validValue, validValue))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessageContaining("cannot exceed 500 cm");

            assertThatThrownBy(() -> new Dimensions(validValue, invalidValue, validValue))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessageContaining("cannot exceed 500 cm");

            assertThatThrownBy(() -> new Dimensions(validValue, validValue, invalidValue))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessageContaining("cannot exceed 500 cm");
        }
    }

    @Nested
    @DisplayName("Volume Calculation Tests")
    class VolumeCalculationTests {

        @Test
        @DisplayName("Should calculate volume correctly")
        void shouldCalculateVolumeCorrectly() {
            // Arrange
            BigDecimal height = new BigDecimal("20");
            BigDecimal width = new BigDecimal("15");
            BigDecimal depth = new BigDecimal("10");
            Dimensions dimensions = new Dimensions(height, width, depth);

            // Act
            BigDecimal volume = dimensions.calculateVolume();

            // Assert
            BigDecimal expectedVolume = new BigDecimal("3000.00");
            assertThat(volume).isEqualByComparingTo(expectedVolume);
        }

        @Test
        @DisplayName("Should calculate volume with decimal precision")
        void shouldCalculateVolumeWithDecimalPrecision() {
            // Arrange
            BigDecimal height = new BigDecimal("20.5");
            BigDecimal width = new BigDecimal("15.25");
            BigDecimal depth = new BigDecimal("10.75");
            Dimensions dimensions = new Dimensions(height, width, depth);

            // Act
            BigDecimal volume = dimensions.calculateVolume();

            // Assert
            // 20.50 * 15.25 * 10.75 = 3359.6875, rounded to 3359.69
            BigDecimal expectedVolume = new BigDecimal("3360.72");
            assertThat(volume).isEqualByComparingTo(expectedVolume);
        }

        @Test
        @DisplayName("Should round volume to 2 decimal places")
        void shouldRoundVolumeTo2DecimalPlaces() {
            // Arrange
            BigDecimal height = new BigDecimal("3.333");
            BigDecimal width = new BigDecimal("3.333");
            BigDecimal depth = new BigDecimal("3.333");
            Dimensions dimensions = new Dimensions(height, width, depth);

            // Act
            BigDecimal volume = dimensions.calculateVolume();

            // Assert
            // 3.33 * 3.33 * 3.33 = 36.926037, rounded to 36.93
            BigDecimal expectedVolume = new BigDecimal("36.93");
            assertThat(volume).isEqualByComparingTo(expectedVolume);
            assertThat(volume.scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should calculate volume for minimum dimensions")
        void shouldCalculateVolumeForMinimumDimensions() {
            // Arrange
            BigDecimal height = new BigDecimal("0.01");
            BigDecimal width = new BigDecimal("0.01");
            BigDecimal depth = new BigDecimal("0.01");
            Dimensions dimensions = new Dimensions(height, width, depth);

            // Act
            BigDecimal volume = dimensions.calculateVolume();

            // Assert
            // 0.01 * 0.01 * 0.01 = 0.000001, rounded to 0.00
            BigDecimal expectedVolume = new BigDecimal("0.00");
            assertThat(volume).isEqualByComparingTo(expectedVolume);
        }

        @Test
        @DisplayName("Should calculate volume for maximum dimensions")
        void shouldCalculateVolumeForMaximumDimensions() {
            // Arrange
            BigDecimal height = new BigDecimal("500");
            BigDecimal width = new BigDecimal("500");
            BigDecimal depth = new BigDecimal("500");
            Dimensions dimensions = new Dimensions(height, width, depth);

            // Act
            BigDecimal volume = dimensions.calculateVolume();

            // Assert
            // 500 * 500 * 500 = 125,000,000
            BigDecimal expectedVolume = new BigDecimal("125000000.00");
            assertThat(volume).isEqualByComparingTo(expectedVolume);
        }
    }

    @Nested
    @DisplayName("Static Factory Method Tests")
    class StaticFactoryMethodTests {

        @Test
        @DisplayName("Should create dimensions from double values")
        void shouldCreateDimensionsFromDoubleValues() {
            // Arrange
            double height = 20.5;
            double width = 15.75;
            double depth = 10.25;

            // Act
            Dimensions dimensions = Dimensions.of(height, width, depth);

            // Assert
            assertThat(dimensions.getHeight()).isEqualByComparingTo("20.50");
            assertThat(dimensions.getWidth()).isEqualByComparingTo("15.75");
            assertThat(dimensions.getDepth()).isEqualByComparingTo("10.25");
        }

        @Test
        @DisplayName("Should handle double precision correctly")
        void shouldHandleDoublePrecisionCorrectly() {
            // Arrange
            double height = 20.555555;
            double width = 15.754321;
            double depth = 10.251234;

            // Act
            Dimensions dimensions = Dimensions.of(height, width, depth);

            // Assert
            assertThat(dimensions.getHeight()).isEqualByComparingTo("20.56");
            assertThat(dimensions.getWidth()).isEqualByComparingTo("15.75");
            assertThat(dimensions.getDepth()).isEqualByComparingTo("10.25");
        }

        @Test
        @DisplayName("Should throw exception for invalid double values")
        void shouldThrowExceptionForInvalidDoubleValues() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> Dimensions.of(-1.0, 10.0, 10.0))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Height must be positive");

            assertThatThrownBy(() -> Dimensions.of(10.0, 0.0, 10.0))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Width must be positive");

            assertThatThrownBy(() -> Dimensions.of(10.0, 10.0, 501.0))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Depth cannot exceed 500 cm");
        }
    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderPatternTests {

        @Test
        @DisplayName("Should create dimensions using builder")
        void shouldCreateDimensionsUsingBuilder() {
            // Arrange & Act
            Dimensions dimensions = Dimensions.builder()
                    .height(new BigDecimal("20.50"))
                    .width(new BigDecimal("15.75"))
                    .depth(new BigDecimal("10.25"))
                    .build();

            // Assert
            assertThat(dimensions.getHeight()).isEqualByComparingTo("20.50");
            assertThat(dimensions.getWidth()).isEqualByComparingTo("15.75");
            assertThat(dimensions.getDepth()).isEqualByComparingTo("10.25");
        }

        @Test
        @DisplayName("Should validate when using builder")
        void shouldValidateWhenUsingBuilder() {
            // Arrange & Act & Assert
            assertThatThrownBy(() ->
                    Dimensions.builder()
                            .height(new BigDecimal("-1"))
                            .width(new BigDecimal("15"))
                            .depth(new BigDecimal("10"))
                            .build())
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Height must be positive");
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should be immutable value object")
        void shouldBeImmutableValueObject() {
            // Arrange
            BigDecimal height = new BigDecimal("20");
            BigDecimal width = new BigDecimal("15");
            BigDecimal depth = new BigDecimal("10");

            // Act
            Dimensions dimensions1 = new Dimensions(height, width, depth);
            Dimensions dimensions2 = new Dimensions(height, width, depth);

            // Assert - Value objects with same values should be equal
            assertThat(dimensions1).isEqualTo(dimensions2);
            assertThat(dimensions1.hashCode()).isEqualTo(dimensions2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal with different values")
        void shouldNotBeEqualWithDifferentValues() {
            // Arrange
            Dimensions dimensions1 = new Dimensions(
                    new BigDecimal("20"), new BigDecimal("15"), new BigDecimal("10"));
            Dimensions dimensions2 = new Dimensions(
                    new BigDecimal("20"), new BigDecimal("15"), new BigDecimal("11"));

            // Act & Assert
            assertThat(dimensions1).isNotEqualTo(dimensions2);
        }
    }
}
