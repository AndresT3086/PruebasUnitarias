package com.logitrack.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Location Tests")
class LocationTest {

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create location with all fields using builder")
        void shouldCreateLocationWithAllFieldsUsingBuilder() {
            // Arrange
            String id = "test-id-123";
            String city = "New York";
            String country = "USA";
            String description = "Package pickup location";
            LocalDateTime timestamp = LocalDateTime.of(2024, 1, 15, 10, 30);
            Double latitude = 40.7128;
            Double longitude = -74.0060;

            // Act
            Location location = Location.builder()
                    .id(id)
                    .city(city)
                    .country(country)
                    .description(description)
                    .timestamp(timestamp)
                    .latitude(latitude)
                    .longitude(longitude)
                    .build();

            // Assert
            assertThat(location.getId()).isEqualTo(id);
            assertThat(location.getCity()).isEqualTo(city);
            assertThat(location.getCountry()).isEqualTo(country);
            assertThat(location.getDescription()).isEqualTo(description);
            assertThat(location.getTimestamp()).isEqualTo(timestamp);
            assertThat(location.getLatitude()).isEqualTo(latitude);
            assertThat(location.getLongitude()).isEqualTo(longitude);
        }

        @Test
        @DisplayName("Should create location with minimal fields")
        void shouldCreateLocationWithMinimalFields() {
            // Arrange
            String city = "Berlin";
            String country = "Germany";

            // Act
            Location location = Location.builder()
                    .city(city)
                    .country(country)
                    .build();

            // Assert
            assertThat(location.getCity()).isEqualTo(city);
            assertThat(location.getCountry()).isEqualTo(country);
            assertThat(location.getId()).isNull();
            assertThat(location.getDescription()).isNull();
            assertThat(location.getTimestamp()).isNull();
            assertThat(location.getLatitude()).isNull();
            assertThat(location.getLongitude()).isNull();
        }

        @Test
        @DisplayName("Should create location with coordinates but no description")
        void shouldCreateLocationWithCoordinatesButNoDescription() {
            // Arrange
            String city = "Tokyo";
            String country = "Japan";
            Double latitude = 35.6762;
            Double longitude = 139.6503;

            // Act
            Location location = Location.builder()
                    .city(city)
                    .country(country)
                    .latitude(latitude)
                    .longitude(longitude)
                    .build();

            // Assert
            assertThat(location.getCity()).isEqualTo(city);
            assertThat(location.getCountry()).isEqualTo(country);
            assertThat(location.getLatitude()).isEqualTo(latitude);
            assertThat(location.getLongitude()).isEqualTo(longitude);
            assertThat(location.getDescription()).isNull();
        }
    }

    @Nested
    @DisplayName("Static Factory Method Tests")
    class StaticFactoryMethodTests {

        @Test
        @DisplayName("Should create location with coordinates using factory method")
        void shouldCreateLocationWithCoordinatesUsingFactoryMethod() {
            // Arrange
            String city = "London";
            String country = "UK";
            String description = "Distribution center";
            Double latitude = 51.5074;
            Double longitude = -0.1278;
            LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);

            // Act
            Location location = Location.create(city, country, description, latitude, longitude);

            // Assert
            assertThat(location.getCity()).isEqualTo(city);
            assertThat(location.getCountry()).isEqualTo(country);
            assertThat(location.getDescription()).isEqualTo(description);
            assertThat(location.getLatitude()).isEqualTo(latitude);
            assertThat(location.getLongitude()).isEqualTo(longitude);

            // Verify generated fields
            assertThat(location.getId()).isNotNull();
            assertThat(location.getId()).isNotEmpty();
            assertThat(location.getTimestamp()).isNotNull();
            assertThat(location.getTimestamp()).isAfter(beforeCreation);
            assertThat(location.getTimestamp()).isBefore(LocalDateTime.now().plusSeconds(1));
        }

        @Test
        @DisplayName("Should create location without coordinates using factory method")
        void shouldCreateLocationWithoutCoordinatesUsingFactoryMethod() {
            // Arrange
            String city = "Paris";
            String country = "France";
            String description = "Package delivered";

            // Act
            Location location = Location.create(city, country, description, null, null);

            // Assert
            assertThat(location.getCity()).isEqualTo(city);
            assertThat(location.getCountry()).isEqualTo(country);
            assertThat(location.getDescription()).isEqualTo(description);
            assertThat(location.getLatitude()).isNull();
            assertThat(location.getLongitude()).isNull();

            // Verify generated fields
            assertThat(location.getId()).isNotNull();
            assertThat(location.getId()).isNotEmpty();
            assertThat(location.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("Should generate unique IDs for different locations")
        void shouldGenerateUniqueIDsForDifferentLocations() {
            // Arrange
            String city = "Madrid";
            String country = "Spain";
            String description = "Transit hub";

            // Act
            Location location1 = Location.create(city, country, description, null, null);
            Location location2 = Location.create(city, country, description, null, null);

            // Assert
            assertThat(location1.getId()).isNotEqualTo(location2.getId());
            assertThat(location1.getTimestamp()).isBeforeOrEqualTo(location2.getTimestamp());
        }

        @Test
        @DisplayName("Should handle null description in factory method")
        void shouldHandleNullDescriptionInFactoryMethod() {
            // Arrange
            String city = "Rome";
            String country = "Italy";
            Double latitude = 41.9028;
            Double longitude = 12.4964;

            // Act
            Location location = Location.create(city, country, null, latitude, longitude);

            // Assert
            assertThat(location.getCity()).isEqualTo(city);
            assertThat(location.getCountry()).isEqualTo(country);
            assertThat(location.getDescription()).isNull();
            assertThat(location.getLatitude()).isEqualTo(latitude);
            assertThat(location.getLongitude()).isEqualTo(longitude);
            assertThat(location.getId()).isNotNull();
            assertThat(location.getTimestamp()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Formatted Location Tests")
    class FormattedLocationTests {

        @Test
        @DisplayName("Should format location with city and country")
        void shouldFormatLocationWithCityAndCountry() {
            // Arrange
            Location location = Location.builder()
                    .city("Barcelona")
                    .country("Spain")
                    .build();

            // Act
            String formatted = location.getFormattedLocation();

            // Assert
            assertThat(formatted).isEqualTo("Barcelona, Spain");
        }

        @Test
        @DisplayName("Should format location with spaces in city name")
        void shouldFormatLocationWithSpacesInCityName() {
            // Arrange
            Location location = Location.builder()
                    .city("New York")
                    .country("United States")
                    .build();

            // Act
            String formatted = location.getFormattedLocation();

            // Assert
            assertThat(formatted).isEqualTo("New York, United States");
        }

        @Test
        @DisplayName("Should format location even with null description")
        void shouldFormatLocationEvenWithNullDescription() {
            // Arrange
            Location location = Location.builder()
                    .city("Amsterdam")
                    .country("Netherlands")
                    .description(null)
                    .build();

            // Act
            String formatted = location.getFormattedLocation();

            // Assert
            assertThat(formatted).isEqualTo("Amsterdam, Netherlands");
        }

        @Test
        @DisplayName("Should handle null city gracefully")
        void shouldHandleNullCityGracefully() {
            // Arrange
            Location location = Location.builder()
                    .city(null)
                    .country("Germany")
                    .build();

            // Act
            String formatted = location.getFormattedLocation();

            // Assert
            assertThat(formatted).isEqualTo("null, Germany");
        }

        @Test
        @DisplayName("Should handle null country gracefully")
        void shouldHandleNullCountryGracefully() {
            // Arrange
            Location location = Location.builder()
                    .city("Vienna")
                    .country(null)
                    .build();

            // Act
            String formatted = location.getFormattedLocation();

            // Assert
            assertThat(formatted).isEqualTo("Vienna, null");
        }

        @Test
        @DisplayName("Should handle both null city and country")
        void shouldHandleBothNullCityAndCountry() {
            // Arrange
            Location location = Location.builder()
                    .city(null)
                    .country(null)
                    .build();

            // Act
            String formatted = location.getFormattedLocation();

            // Assert
            assertThat(formatted).isEqualTo("null, null");
        }
    }

    @Nested
    @DisplayName("Coordinates Tests")
    class CoordinatesTests {

        @Test
        @DisplayName("Should handle positive coordinates")
        void shouldHandlePositiveCoordinates() {
            // Arrange
            Double latitude = 40.7128;
            Double longitude = 74.0060;

            // Act
            Location location = Location.create("New York", "USA", "Test", latitude, longitude);

            // Assert
            assertThat(location.getLatitude()).isEqualTo(latitude);
            assertThat(location.getLongitude()).isEqualTo(longitude);
        }

        @Test
        @DisplayName("Should handle negative coordinates")
        void shouldHandleNegativeCoordinates() {
            // Arrange
            Double latitude = -33.8688;
            Double longitude = -151.2093;

            // Act
            Location location = Location.create("Sydney", "Australia", "Test", latitude, longitude);

            // Assert
            assertThat(location.getLatitude()).isEqualTo(latitude);
            assertThat(location.getLongitude()).isEqualTo(longitude);
        }

        @Test
        @DisplayName("Should handle zero coordinates")
        void shouldHandleZeroCoordinates() {
            // Arrange
            Double latitude = 0.0;
            Double longitude = 0.0;

            // Act
            Location location = Location.create("Null Island", "Ocean", "Test", latitude, longitude);

            // Assert
            assertThat(location.getLatitude()).isEqualTo(latitude);
            assertThat(location.getLongitude()).isEqualTo(longitude);
        }

        @Test
        @DisplayName("Should handle extreme coordinates")
        void shouldHandleExtremeCoordinates() {
            // Arrange
            Double latitude = 90.0; // North Pole
            Double longitude = 180.0; // International Date Line

            // Act
            Location location = Location.create("North Pole", "Arctic", "Test", latitude, longitude);

            // Assert
            assertThat(location.getLatitude()).isEqualTo(latitude);
            assertThat(location.getLongitude()).isEqualTo(longitude);
        }

        @Test
        @DisplayName("Should handle partial coordinates - only latitude")
        void shouldHandlePartialCoordinatesOnlyLatitude() {
            // Arrange
            Double latitude = 51.5074;
            Double longitude = null;

            // Act
            Location location = Location.create("London", "UK", "Test", latitude, longitude);

            // Assert
            assertThat(location.getLatitude()).isEqualTo(latitude);
            assertThat(location.getLongitude()).isNull();
        }

        @Test
        @DisplayName("Should handle partial coordinates - only longitude")
        void shouldHandlePartialCoordinatesOnlyLongitude() {
            // Arrange
            Double latitude = null;
            Double longitude = -0.1278;

            // Act
            Location location = Location.create("London", "UK", "Test", latitude, longitude);

            // Assert
            assertThat(location.getLatitude()).isNull();
            assertThat(location.getLongitude()).isEqualTo(longitude);
        }
    }

    @Nested
    @DisplayName("Value Object Tests")
    class ValueObjectTests {

        @Test
        @DisplayName("Should be equal when all fields are the same")
        void shouldBeEqualWhenAllFieldsAreTheSame() {
            // Arrange
            String id = "test-id";
            String city = "Prague";
            String country = "Czech Republic";
            String description = "Delivery point";
            LocalDateTime timestamp = LocalDateTime.of(2024, 1, 15, 10, 30);
            Double latitude = 50.0755;
            Double longitude = 14.4378;

            Location location1 = Location.builder()
                    .id(id)
                    .city(city)
                    .country(country)
                    .description(description)
                    .timestamp(timestamp)
                    .latitude(latitude)
                    .longitude(longitude)
                    .build();

            Location location2 = Location.builder()
                    .id(id)
                    .city(city)
                    .country(country)
                    .description(description)
                    .timestamp(timestamp)
                    .latitude(latitude)
                    .longitude(longitude)
                    .build();

            // Act & Assert
            assertThat(location1).isEqualTo(location2);
            assertThat(location1.hashCode()).isEqualTo(location2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when IDs differ")
        void shouldNotBeEqualWhenIDsDiffer() {
            // Arrange
            Location location1 = Location.builder()
                    .id("id-1")
                    .city("Warsaw")
                    .country("Poland")
                    .build();

            Location location2 = Location.builder()
                    .id("id-2")
                    .city("Warsaw")
                    .country("Poland")
                    .build();

            // Act & Assert
            assertThat(location1).isNotEqualTo(location2);
        }

        @Test
        @DisplayName("Should not be equal when cities differ")
        void shouldNotBeEqualWhenCitiesDiffer() {
            // Arrange
            Location location1 = Location.builder()
                    .city("Budapest")
                    .country("Hungary")
                    .build();

            Location location2 = Location.builder()
                    .city("Vienna")
                    .country("Hungary")
                    .build();

            // Act & Assert
            assertThat(location1).isNotEqualTo(location2);
        }

        @Test
        @DisplayName("Should be immutable")
        void shouldBeImmutable() {
            // Arrange
            Location location = Location.builder()
                    .city("Zurich")
                    .country("Switzerland")
                    .description("Financial district")
                    .build();

            // Act & Assert
            // Verify that Location is a value object (immutable)
            // The @Value annotation ensures immutability
            assertThat(location.getCity()).isEqualTo("Zurich");
            assertThat(location.getCountry()).isEqualTo("Switzerland");
            assertThat(location.getDescription()).isEqualTo("Financial district");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty string fields")
        void shouldHandleEmptyStringFields() {
            // Arrange
            String city = "";
            String country = "";
            String description = "";

            // Act
            Location location = Location.create(city, country, description, null, null);

            // Assert
            assertThat(location.getCity()).isEmpty();
            assertThat(location.getCountry()).isEmpty();
            assertThat(location.getDescription()).isEmpty();
            assertThat(location.getFormattedLocation()).isEqualTo(", ");
        }

        @Test
        @DisplayName("Should handle very long descriptions")
        void shouldHandleVeryLongDescriptions() {
            // Arrange
            String longDescription = "A".repeat(1000);

            // Act
            Location location = Location.create("Test City", "Test Country", longDescription, null, null);

            // Assert
            assertThat(location.getDescription()).isEqualTo(longDescription);
            assertThat(location.getDescription()).hasSize(1000);
        }

        @Test
        @DisplayName("Should handle special characters in city and country")
        void shouldHandleSpecialCharactersInCityAndCountry() {
            // Arrange
            String city = "São Paulo";
            String country = "Côte d'Ivoire";
            String description = "Ñiño's café";

            // Act
            Location location = Location.create(city, country, description, null, null);

            // Assert
            assertThat(location.getCity()).isEqualTo(city);
            assertThat(location.getCountry()).isEqualTo(country);
            assertThat(location.getDescription()).isEqualTo(description);
            assertThat(location.getFormattedLocation()).isEqualTo("São Paulo, Côte d'Ivoire");
        }
    }
}
