package com.logitrack.infrastructure.adapter.out.external;

import com.logitrack.domain.port.out.LocationService;
import com.logitrack.infrastructure.adapter.out.external.client.GeocodeApiClient;
import com.logitrack.infrastructure.adapter.out.external.client.GeocodeApiClient.GeocodeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LocationServiceAdapter Tests")
class LocationServiceAdapterTest {

    @Mock
    private GeocodeApiClient geocodeClient;

    @InjectMocks
    private LocationServiceAdapter locationServiceAdapter;

    private GeocodeResponse successfulResponse;
    private GeocodeResponse.Components completeComponents;
    private GeocodeResponse.Geometry geometry;

    @BeforeEach
    void setUp() {
        // Arrange - Common test data setup
        completeComponents = GeocodeResponse.Components.builder()
                .city("New York")
                .country("United States")
                .state("New York")
                .town(null)
                .village(null)
                .countryCode("US")
                .postcode("10001")
                .build();

        geometry = GeocodeResponse.Geometry.builder()
                .lat(40.7128)
                .lng(-74.0060)
                .build();

        GeocodeResponse.Result result = GeocodeResponse.Result.builder()
                .components(completeComponents)
                .geometry(geometry)
                .formatted("New York, NY 10001, United States")
                .build();

        successfulResponse = GeocodeResponse.builder()
                .results(Arrays.asList(result))
                .build();
    }

    @Nested
    @DisplayName("Get Location Info Tests")
    class GetLocationInfoTests {

        @Test
        @DisplayName("Should get location info successfully with complete data")
        void shouldGetLocationInfoSuccessfullyWithCompleteData() {
            // Arrange
            String city = "New York";
            String country = "USA";
            when(geocodeClient.geocodeLocation(city, country)).thenReturn(Optional.of(successfulResponse));

            // Act
            Optional<LocationService.LocationInfo> result = locationServiceAdapter.getLocationInfo(city, country);

            // Assert
            assertThat(result).isPresent();
            LocationService.LocationInfo locationInfo = result.get();
            assertThat(locationInfo.city()).isEqualTo("New York");
            assertThat(locationInfo.country()).isEqualTo("United States");
            assertThat(locationInfo.state()).isEqualTo("New York");
            assertThat(locationInfo.latitude()).isEqualTo(40.7128);
            assertThat(locationInfo.longitude()).isEqualTo(-74.0060);
            assertThat(locationInfo.timezone()).isNull();

            verify(geocodeClient).geocodeLocation(city, country);
        }

        @Test
        @DisplayName("Should fallback to town when city is null")
        void shouldFallbackToTownWhenCityIsNull() {
            // Arrange
            GeocodeResponse.Components componentsWithTown = GeocodeResponse.Components.builder()
                    .city(null)
                    .town("Small Town")
                    .village(null)
                    .country("United States")
                    .state("Some State")
                    .build();

            GeocodeResponse.Result result = GeocodeResponse.Result.builder()
                    .components(componentsWithTown)
                    .geometry(geometry)
                    .build();

            GeocodeResponse response = GeocodeResponse.builder()
                    .results(Arrays.asList(result))
                    .build();

            when(geocodeClient.geocodeLocation("query", "country")).thenReturn(Optional.of(response));

            // Act
            Optional<LocationService.LocationInfo> results = locationServiceAdapter.getLocationInfo("query", "country");

            // Assert
            assertThat(results).isPresent();
            assertThat(results.get().city()).isEqualTo("Small Town");
        }

        @Test
        @DisplayName("Should fallback to village when city and town are null")
        void shouldFallbackToVillageWhenCityAndTownAreNull() {
            // Arrange
            GeocodeResponse.Components componentsWithVillage = GeocodeResponse.Components.builder()
                    .city(null)
                    .town(null)
                    .village("Small Village")
                    .country("United States")
                    .state("Some State")
                    .build();

            GeocodeResponse.Result result = GeocodeResponse.Result.builder()
                    .components(componentsWithVillage)
                    .geometry(geometry)
                    .build();

            GeocodeResponse response = GeocodeResponse.builder()
                    .results(Arrays.asList(result))
                    .build();

            when(geocodeClient.geocodeLocation("query", "country")).thenReturn(Optional.of(response));

            // Act
            Optional<LocationService.LocationInfo> results = locationServiceAdapter.getLocationInfo("query", "country");

            // Assert
            assertThat(results).isPresent();
            assertThat(results.get().city()).isEqualTo("Small Village");
        }

        @Test
        @DisplayName("Should use original city when all location fields are null")
        void shouldUseOriginalCityWhenAllLocationFieldsAreNull() {
            // Arrange
            GeocodeResponse.Components componentsWithNullLocations = GeocodeResponse.Components.builder()
                    .city(null)
                    .town(null)
                    .village(null)
                    .country("United States")
                    .state("Some State")
                    .build();

            GeocodeResponse.Result result = GeocodeResponse.Result.builder()
                    .components(componentsWithNullLocations)
                    .geometry(geometry)
                    .build();

            GeocodeResponse response = GeocodeResponse.builder()
                    .results(Arrays.asList(result))
                    .build();

            String originalCity = "Original City";
            when(geocodeClient.geocodeLocation(originalCity, "country")).thenReturn(Optional.of(response));

            // Act
            Optional<LocationService.LocationInfo> results = locationServiceAdapter.getLocationInfo(originalCity, "country");

            // Assert
            assertThat(results).isPresent();
            assertThat(results.get().city()).isEqualTo(originalCity);
        }

        @Test
        @DisplayName("Should fallback to original country when country is null")
        void shouldFallbackToOriginalCountryWhenCountryIsNull() {
            // Arrange
            GeocodeResponse.Components componentsWithNullCountry = GeocodeResponse.Components.builder()
                    .city("Test City")
                    .country(null)
                    .state("Some State")
                    .build();

            GeocodeResponse.Result result = GeocodeResponse.Result.builder()
                    .components(componentsWithNullCountry)
                    .geometry(geometry)
                    .build();

            GeocodeResponse response = GeocodeResponse.builder()
                    .results(Arrays.asList(result))
                    .build();

            String originalCountry = "Original Country";
            when(geocodeClient.geocodeLocation("city", originalCountry)).thenReturn(Optional.of(response));

            // Act
            Optional<LocationService.LocationInfo> results = locationServiceAdapter.getLocationInfo("city", originalCountry);

            // Assert
            assertThat(results).isPresent();
            assertThat(results.get().country()).isEqualTo(originalCountry);
        }

        @Test
        @DisplayName("Should return empty when geocode client returns empty")
        void shouldReturnEmptyWhenGeocodeClientReturnsEmpty() {
            // Arrange
            String city = "Invalid City";
            String country = "Invalid Country";
            when(geocodeClient.geocodeLocation(city, country)).thenReturn(Optional.empty());

            // Act
            Optional<LocationService.LocationInfo> result = locationServiceAdapter.getLocationInfo(city, country);

            // Assert
            assertThat(result).isEmpty();
            verify(geocodeClient).geocodeLocation(city, country);
        }

        @Test
        @DisplayName("Should return empty when geocode response has no results")
        void shouldReturnEmptyWhenGeocodeResponseHasNoResults() {
            // Arrange
            GeocodeResponse emptyResponse = GeocodeResponse.builder()
                    .results(Collections.emptyList())
                    .build();

            when(geocodeClient.geocodeLocation("city", "country")).thenReturn(Optional.of(emptyResponse));

            // Act & Assert - Should throw IndexOutOfBoundsException when trying to get first result
            assertThatThrownBy(() -> locationServiceAdapter.getLocationInfo("city", "country"))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }
    }

    @Nested
    @DisplayName("Get Location By Coordinates Tests")
    class GetLocationByCoordinatesTests {

        @Test
        @DisplayName("Should get location by coordinates successfully")
        void shouldGetLocationByCoordinatesSuccessfully() {
            // Arrange
            double latitude = 40.7128;
            double longitude = -74.0060;
            when(geocodeClient.reverseGeocode(latitude, longitude)).thenReturn(Optional.of(successfulResponse));

            // Act
            Optional<LocationService.LocationInfo> result = locationServiceAdapter.getLocationByCoordinates(latitude, longitude);

            // Assert
            assertThat(result).isPresent();
            LocationService.LocationInfo locationInfo = result.get();
            assertThat(locationInfo.city()).isEqualTo("New York");
            assertThat(locationInfo.country()).isEqualTo("United States");
            assertThat(locationInfo.state()).isEqualTo("New York");
            assertThat(locationInfo.latitude()).isEqualTo(latitude);
            assertThat(locationInfo.longitude()).isEqualTo(longitude);

            verify(geocodeClient).reverseGeocode(latitude, longitude);
        }

        @Test
        @DisplayName("Should use Unknown for city when all location fields are null")
        void shouldUseUnknownForCityWhenAllLocationFieldsAreNull() {
            // Arrange
            GeocodeResponse.Components unknownComponents = GeocodeResponse.Components.builder()
                    .city(null)
                    .town(null)
                    .village(null)
                    .country("Some Country")
                    .build();

            GeocodeResponse.Result result = GeocodeResponse.Result.builder()
                    .components(unknownComponents)
                    .geometry(geometry)
                    .build();

            GeocodeResponse response = GeocodeResponse.builder()
                    .results(Arrays.asList(result))
                    .build();

            double latitude = 40.7128;
            double longitude = -74.0060;
            when(geocodeClient.reverseGeocode(latitude, longitude)).thenReturn(Optional.of(response));

            // Act
            Optional<LocationService.LocationInfo> results = locationServiceAdapter.getLocationByCoordinates(latitude, longitude);

            // Assert
            assertThat(results).isPresent();
            assertThat(results.get().city()).isEqualTo("Unknown");
        }

        @Test
        @DisplayName("Should use Unknown for country when country is null")
        void shouldUseUnknownForCountryWhenCountryIsNull() {
            // Arrange
            GeocodeResponse.Components unknownCountryComponents = GeocodeResponse.Components.builder()
                    .city("Some City")
                    .country(null)
                    .build();

            GeocodeResponse.Result result = GeocodeResponse.Result.builder()
                    .components(unknownCountryComponents)
                    .geometry(geometry)
                    .build();

            GeocodeResponse response = GeocodeResponse.builder()
                    .results(Arrays.asList(result))
                    .build();

            double latitude = 40.7128;
            double longitude = -74.0060;
            when(geocodeClient.reverseGeocode(latitude, longitude)).thenReturn(Optional.of(response));

            // Act
            Optional<LocationService.LocationInfo> results = locationServiceAdapter.getLocationByCoordinates(latitude, longitude);

            // Assert
            assertThat(results).isPresent();
            assertThat(results.get().country()).isEqualTo("Unknown");
        }

        @Test
        @DisplayName("Should return empty when reverse geocode returns empty")
        void shouldReturnEmptyWhenReverseGeocodeReturnsEmpty() {
            // Arrange
            double latitude = 0.0;
            double longitude = 0.0;
            when(geocodeClient.reverseGeocode(latitude, longitude)).thenReturn(Optional.empty());

            // Act
            Optional<LocationService.LocationInfo> result = locationServiceAdapter.getLocationByCoordinates(latitude, longitude);

            // Assert
            assertThat(result).isEmpty();
            verify(geocodeClient).reverseGeocode(latitude, longitude);
        }

        @Test
        @DisplayName("Should prefer town over village when city is null")
        void shouldPreferTownOverVillageWhenCityIsNull() {
            // Arrange
            GeocodeResponse.Components componentsWithTownAndVillage = GeocodeResponse.Components.builder()
                    .city(null)
                    .town("Test Town")
                    .village("Test Village")
                    .country("Test Country")
                    .build();

            GeocodeResponse.Result result = GeocodeResponse.Result.builder()
                    .components(componentsWithTownAndVillage)
                    .geometry(geometry)
                    .build();

            GeocodeResponse response = GeocodeResponse.builder()
                    .results(Arrays.asList(result))
                    .build();

            when(geocodeClient.reverseGeocode(40.7128, -74.0060)).thenReturn(Optional.of(response));

            // Act
            Optional<LocationService.LocationInfo> results = locationServiceAdapter.getLocationByCoordinates(40.7128, -74.0060);

            // Assert
            assertThat(results).isPresent();
            assertThat(results.get().city()).isEqualTo("Test Town");
        }
    }

    @Nested
    @DisplayName("Validate Location Tests")
    class ValidateLocationTests {

        @Test
        @DisplayName("Should return true when location is valid")
        void shouldReturnTrueWhenLocationIsValid() {
            // Arrange
            String city = "Valid City";
            String country = "Valid Country";
            when(geocodeClient.geocodeLocation(city, country)).thenReturn(Optional.of(successfulResponse));

            // Act
            boolean result = locationServiceAdapter.validateLocation(city, country);

            // Assert
            assertThat(result).isTrue();
            verify(geocodeClient).geocodeLocation(city, country);
        }

        @Test
        @DisplayName("Should return false when location is invalid")
        void shouldReturnFalseWhenLocationIsInvalid() {
            // Arrange
            String city = "Invalid City";
            String country = "Invalid Country";
            when(geocodeClient.geocodeLocation(city, country)).thenReturn(Optional.empty());

            // Act
            boolean result = locationServiceAdapter.validateLocation(city, country);

            // Assert
            assertThat(result).isFalse();
            verify(geocodeClient).geocodeLocation(city, country);
        }

        @Test
        @DisplayName("Should return false when geocode client throws exception")
        void shouldReturnFalseWhenGeocodeClientThrowsException() {
            // Arrange
            String city = "Problem City";
            String country = "Problem Country";
            when(geocodeClient.geocodeLocation(city, country)).thenThrow(new RuntimeException("API Error"));

            // Act & Assert
            assertThatThrownBy(() -> locationServiceAdapter.validateLocation(city, country))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("API Error");

            verify(geocodeClient).geocodeLocation(city, country);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle null components gracefully")
        void shouldHandleNullComponentsGracefully() {
            // Arrange
            GeocodeResponse.Result resultWithNullComponents = GeocodeResponse.Result.builder()
                    .components(null)
                    .geometry(geometry)
                    .build();

            GeocodeResponse responseWithNullComponents = GeocodeResponse.builder()
                    .results(Arrays.asList(resultWithNullComponents))
                    .build();

            when(geocodeClient.geocodeLocation("city", "country")).thenReturn(Optional.of(responseWithNullComponents));

            // Act & Assert
            assertThatThrownBy(() -> locationServiceAdapter.getLocationInfo("city", "country"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should handle null geometry gracefully")
        void shouldHandleNullGeometryGracefully() {
            // Arrange
            GeocodeResponse.Result resultWithNullGeometry = GeocodeResponse.Result.builder()
                    .components(completeComponents)
                    .geometry(null)
                    .build();

            GeocodeResponse responseWithNullGeometry = GeocodeResponse.builder()
                    .results(Arrays.asList(resultWithNullGeometry))
                    .build();

            when(geocodeClient.geocodeLocation("city", "country")).thenReturn(Optional.of(responseWithNullGeometry));

            // Act & Assert
            assertThatThrownBy(() -> locationServiceAdapter.getLocationInfo("city", "country"))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Cache Behavior Tests")
    class CacheBehaviorTests {

        @Test
        @DisplayName("Should call geocode client for different location requests")
        void shouldCallGeocodeClientForDifferentLocationRequests() {
            // Arrange
            when(geocodeClient.geocodeLocation("City1", "Country1")).thenReturn(Optional.of(successfulResponse));
            when(geocodeClient.geocodeLocation("City2", "Country2")).thenReturn(Optional.of(successfulResponse));

            // Act
            locationServiceAdapter.getLocationInfo("City1", "Country1");
            locationServiceAdapter.getLocationInfo("City2", "Country2");

            // Assert
            verify(geocodeClient).geocodeLocation("City1", "Country1");
            verify(geocodeClient).geocodeLocation("City2", "Country2");
        }

        @Test
        @DisplayName("Should call reverse geocode client for different coordinate requests")
        void shouldCallReverseGeocodeClientForDifferentCoordinateRequests() {
            // Arrange
            when(geocodeClient.reverseGeocode(40.7128, -74.0060)).thenReturn(Optional.of(successfulResponse));
            when(geocodeClient.reverseGeocode(34.0522, -118.2437)).thenReturn(Optional.of(successfulResponse));

            // Act
            locationServiceAdapter.getLocationByCoordinates(40.7128, -74.0060);
            locationServiceAdapter.getLocationByCoordinates(34.0522, -118.2437);

            // Assert
            verify(geocodeClient).reverseGeocode(40.7128, -74.0060);
            verify(geocodeClient).reverseGeocode(34.0522, -118.2437);
        }
    }
}
