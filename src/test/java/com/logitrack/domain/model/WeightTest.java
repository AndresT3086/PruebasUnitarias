package com.logitrack.domain.model;

import com.logitrack.domain.exception.InvalidPackageDataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Weight Tests")
class WeightTest {

    @Nested
    @DisplayName("Kilograms Factory Method Tests")
    class KilogramsFactoryMethodTests {

        @Test
        @DisplayName("Should create weight from kilograms")
        void shouldCreateWeightFromKilograms() {
            // Arrange
            double kg = 25.5;

            // Act
            Weight weight = Weight.ofKilograms(kg);

            // Assert
            assertThat(weight.getValueInKg()).isEqualByComparingTo("25.500");
            assertThat(weight.toKilograms()).isEqualTo(25.5);
        }

        @Test
        @DisplayName("Should round to 3 decimal places")
        void shouldRoundTo3DecimalPlaces() {
            // Arrange
            double kg = 25.55555;

            // Act
            Weight weight = Weight.ofKilograms(kg);

            // Assert
            assertThat(weight.getValueInKg()).isEqualByComparingTo("25.556"); // Rounded up
            assertThat(weight.getValueInKg().scale()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should handle exact half values with HALF_UP rounding")
        void shouldHandleExactHalfValuesWithHalfUpRounding() {
            // Arrange
            double kg = 25.5555; // Should round to 25.556

            // Act
            Weight weight = Weight.ofKilograms(kg);

            // Assert
            assertThat(weight.getValueInKg()).isEqualByComparingTo("25.556");
        }

        @Test
        @DisplayName("Should create weight with very small value")
        void shouldCreateWeightWithVerySmallValue() {
            // Arrange
            double kg = 0.001;

            // Act
            Weight weight = Weight.ofKilograms(kg);

            // Assert
            assertThat(weight.getValueInKg()).isEqualByComparingTo("0.001");
            assertThat(weight.toKilograms()).isEqualTo(0.001);
        }
    }

    @Nested
    @DisplayName("Grams Factory Method Tests")
    class GramsFactoryMethodTests {

        @Test
        @DisplayName("Should create weight from grams")
        void shouldCreateWeightFromGrams() {
            // Arrange
            double grams = 2500.0;

            // Act
            Weight weight = Weight.ofGrams(grams);

            // Assert
            assertThat(weight.getValueInKg()).isEqualByComparingTo("2.500");
            assertThat(weight.toKilograms()).isEqualTo(2.5);
            assertThat(weight.toGrams()).isEqualTo(2500.0);
        }

        @Test
        @DisplayName("Should convert grams to kilograms correctly")
        void shouldConvertGramsToKilogramsCorrectly() {
            // Arrange
            double grams = 1500.0;

            // Act
            Weight weight = Weight.ofGrams(grams);

            // Assert
            assertThat(weight.getValueInKg()).isEqualByComparingTo("1.500");
            assertThat(weight.toKilograms()).isEqualTo(1.5);
        }

        @Test
        @DisplayName("Should handle small gram values")
        void shouldHandleSmallGramValues() {
            // Arrange
            double grams = 1.0; // 1 gram = 0.001 kg

            // Act
            Weight weight = Weight.ofGrams(grams);

            // Assert
            assertThat(weight.getValueInKg()).isEqualByComparingTo("0.001");
            assertThat(weight.toGrams()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should round grams conversion to 3 decimal places")
        void shouldRoundGramsConversionTo3DecimalPlaces() {
            // Arrange
            double grams = 1.2345; // Should result in 0.0012345 kg, rounded to 0.001 kg

            // Act
            Weight weight = Weight.ofGrams(grams);

            // Assert
            assertThat(weight.getValueInKg()).isEqualByComparingTo("0.001");
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception for zero weight")
        void shouldThrowExceptionForZeroWeight() {
            // Arrange
            double zeroKg = 0.0;

            // Act & Assert
            assertThatThrownBy(() -> Weight.ofKilograms(zeroKg))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Weight must be positive");
        }

        @Test
        @DisplayName("Should throw exception for negative weight")
        void shouldThrowExceptionForNegativeWeight() {
            // Arrange
            double negativeKg = -5.0;

            // Act & Assert
            assertThatThrownBy(() -> Weight.ofKilograms(negativeKg))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Weight must be positive");
        }

        @Test
        @DisplayName("Should throw exception for weight exceeding maximum")
        void shouldThrowExceptionForWeightExceedingMaximum() {
            // Arrange
            double excessiveKg = 1001.0;

            // Act & Assert
            assertThatThrownBy(() -> Weight.ofKilograms(excessiveKg))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Weight cannot exceed 1000 kg");
        }

        @Test
        @DisplayName("Should throw exception for zero grams")
        void shouldThrowExceptionForZeroGrams() {
            // Arrange
            double zeroGrams = 0.0;

            // Act & Assert
            assertThatThrownBy(() -> Weight.ofGrams(zeroGrams))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Weight must be positive");
        }

        @Test
        @DisplayName("Should throw exception for negative grams")
        void shouldThrowExceptionForNegativeGrams() {
            // Arrange
            double negativeGrams = -100.0;

            // Act & Assert
            assertThatThrownBy(() -> Weight.ofGrams(negativeGrams))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Weight must be positive");
        }

        @Test
        @DisplayName("Should throw exception for excessive grams")
        void shouldThrowExceptionForExcessiveGrams() {
            // Arrange
            double excessiveGrams = 1500000.0; // > 1000 kg

            // Act & Assert
            assertThatThrownBy(() -> Weight.ofGrams(excessiveGrams))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Weight cannot exceed 1000 kg");
        }
    }

    @Nested
    @DisplayName("Boundary Value Tests")
    class BoundaryValueTests {

        @Test
        @DisplayName("Should accept minimum positive weight")
        void shouldAcceptMinimumPositiveWeight() {
            // Arrange
            double minKg = 0.001;

            // Act
            Weight weight = Weight.ofKilograms(minKg);

            // Assert
            assertThat(weight.getValueInKg()).isEqualByComparingTo("0.001");
        }

        @Test
        @DisplayName("Should accept maximum allowed weight")
        void shouldAcceptMaximumAllowedWeight() {
            // Arrange
            double maxKg = 1000.0;

            // Act
            Weight weight = Weight.ofKilograms(maxKg);

            // Assert
            assertThat(weight.getValueInKg()).isEqualByComparingTo("1000.000");
        }

        @Test
        @DisplayName("Should reject weight just over maximum")
        void shouldRejectWeightJustOverMaximum() {
            // Arrange
            double justOverMax = 1000.001;

            // Act & Assert
            assertThatThrownBy(() -> Weight.ofKilograms(justOverMax))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Weight cannot exceed 1000 kg");
        }

        @ParameterizedTest
        @ValueSource(doubles = {-1000.0, -1.0, -0.001, 0.0})
        @DisplayName("Should reject non-positive weights")
        void shouldRejectNonPositiveWeights(double invalidWeight) {
            // Arrange - invalidWeight from parameter

            // Act & Assert
            assertThatThrownBy(() -> Weight.ofKilograms(invalidWeight))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Weight must be positive");
        }

        @ParameterizedTest
        @ValueSource(doubles = {1000.1, 1500.0, 2000.0, 10000.0})
        @DisplayName("Should reject weights exceeding maximum")
        void shouldRejectWeightsExceedingMaximum(double excessiveWeight) {
            // Arrange - excessiveWeight from parameter

            // Act & Assert
            assertThatThrownBy(() -> Weight.ofKilograms(excessiveWeight))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Weight cannot exceed 1000 kg");
        }
    }

    @Nested
    @DisplayName("Conversion Tests")
    class ConversionTests {

        @Test
        @DisplayName("Should convert to kilograms correctly")
        void shouldConvertToKilogramsCorrectly() {
            // Arrange
            Weight weight = Weight.ofKilograms(15.525);

            // Act
            double kg = weight.toKilograms();

            // Assert
            assertThat(kg).isEqualTo(15.525);
        }

        @Test
        @DisplayName("Should convert to grams correctly")
        void shouldConvertToGramsCorrectly() {
            // Arrange
            Weight weight = Weight.ofKilograms(2.5);

            // Act
            double grams = weight.toGrams();

            // Assert
            assertThat(grams).isEqualTo(2500.0);
        }

        @Test
        @DisplayName("Should maintain precision in conversions")
        void shouldMaintainPrecisionInConversions() {
            // Arrange
            double originalGrams = 1234.567;
            Weight weight = Weight.ofGrams(originalGrams);

            // Act
            double convertedGrams = weight.toGrams();

            // Assert
            // Note: Due to rounding to 3 decimal places in kg, some precision may be lost
            assertThat(convertedGrams).isCloseTo(originalGrams, within(0.5));
        }

        @Test
        @DisplayName("Should handle round trip conversion accurately")
        void shouldHandleRoundTripConversionAccurately() {
            // Arrange
            Weight weight = Weight.ofKilograms(5.123);

            // Act
            double kg = weight.toKilograms();
            double grams = weight.toGrams();

            // Assert
            assertThat(kg).isEqualTo(5.123);
            assertThat(grams).isEqualTo(5123.0);
        }
    }

    @Nested
    @DisplayName("Precision and Rounding Tests")
    class PrecisionAndRoundingTests {

        @Test
        @DisplayName("Should handle precision with very small values")
        void shouldHandlePrecisionWithVerySmallValues() {
            // Arrange
            double verySmallKg = 0.0005; // Should round to 0.001

            // Act
            Weight weight = Weight.ofKilograms(verySmallKg);

            // Assert
            assertThat(weight.getValueInKg()).isEqualByComparingTo("0.001");
        }

        @Test
        @DisplayName("Should round down when appropriate")
        void shouldRoundDownWhenAppropriate() {
            // Arrange
            double kg = 25.1234; // Should round to 25.123

            // Act
            Weight weight = Weight.ofKilograms(kg);

            // Assert
            assertThat(weight.getValueInKg()).isEqualByComparingTo("25.123");
        }

        @Test
        @DisplayName("Should round up when appropriate")
        void shouldRoundUpWhenAppropriate() {
            // Arrange
            double kg = 25.1236; // Should round to 25.124

            // Act
            Weight weight = Weight.ofKilograms(kg);

            // Assert
            assertThat(weight.getValueInKg()).isEqualByComparingTo("25.124");
        }

        @Test
        @DisplayName("Should maintain 3 decimal places consistently")
        void shouldMaintain3DecimalPlacesConsistently() {
            // Arrange & Act
            Weight weight1 = Weight.ofKilograms(10);
            Weight weight2 = Weight.ofKilograms(10.1);
            Weight weight3 = Weight.ofKilograms(10.12);
            Weight weight4 = Weight.ofKilograms(10.123);

            // Assert
            assertThat(weight1.getValueInKg()).isEqualByComparingTo("10.000");
            assertThat(weight2.getValueInKg()).isEqualByComparingTo("10.100");
            assertThat(weight3.getValueInKg()).isEqualByComparingTo("10.120");
            assertThat(weight4.getValueInKg()).isEqualByComparingTo("10.123");

            assertThat(weight1.getValueInKg().scale()).isEqualTo(3);
            assertThat(weight2.getValueInKg().scale()).isEqualTo(3);
            assertThat(weight3.getValueInKg().scale()).isEqualTo(3);
            assertThat(weight4.getValueInKg().scale()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Value Object Tests")
    class ValueObjectTests {

        @Test
        @DisplayName("Should be equal when values are same")
        void shouldBeEqualWhenValuesAreSame() {
            // Arrange
            Weight weight1 = Weight.ofKilograms(15.5);
            Weight weight2 = Weight.ofKilograms(15.5);

            // Act & Assert
            assertThat(weight1).isEqualTo(weight2);
            assertThat(weight1.hashCode()).isEqualTo(weight2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when values are different")
        void shouldNotBeEqualWhenValuesAreDifferent() {
            // Arrange
            Weight weight1 = Weight.ofKilograms(15.5);
            Weight weight2 = Weight.ofKilograms(15.6);

            // Act & Assert
            assertThat(weight1).isNotEqualTo(weight2);
        }

        @Test
        @DisplayName("Should be immutable")
        void shouldBeImmutable() {
            // Arrange
            Weight weight = Weight.ofKilograms(10.5);

            // Act & Assert
            assertThat(weight.getValueInKg()).isEqualByComparingTo("10.500");
            // Weight is immutable due to @Value annotation - cannot modify after creation
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle Double.MIN_VALUE appropriately")
        void shouldHandleDoubleMinValueAppropriately() {
            // Arrange
            double minValue = Double.MIN_VALUE; // Very small positive number

            // Act
            Weight weight = Weight.ofKilograms(minValue);

            // Assert
            assertThat(weight.getValueInKg()).isEqualByComparingTo("0.000");
        }

        @Test
        @DisplayName("Should handle maximum allowed weight exactly")
        void shouldHandleMaximumAllowedWeightExactly() {
            // Arrange
            double exactMax = 1000.0;

            // Act
            Weight weight = Weight.ofKilograms(exactMax);

            // Assert
            assertThat(weight.getValueInKg()).isEqualByComparingTo("1000.000");
            assertThat(weight.toGrams()).isEqualTo(1000000.0);
        }
    }
}
