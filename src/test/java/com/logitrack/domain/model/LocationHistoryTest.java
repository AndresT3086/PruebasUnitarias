package com.logitrack.domain.model;

import com.logitrack.domain.exception.InvalidPackageDataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LocationHistory Tests")
class LocationHistoryTest {

    @Mock
    private Location mockLocation1;

    @Mock
    private Location mockLocation2;

    @Mock
    private Location mockLocation3;

    private LocationHistory locationHistory;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        // Arrange - Common setup
        locationHistory = new LocationHistory();
        baseTime = LocalDateTime.of(2024, 1, 15, 10, 0);

        // Setup mock locations with lenient() para evitar unnecessary stubbing
        lenient().when(mockLocation1.getTimestamp()).thenReturn(baseTime);
        lenient().when(mockLocation1.getFormattedLocation()).thenReturn("New York, USA");

        lenient().when(mockLocation2.getTimestamp()).thenReturn(baseTime.plusHours(2));
        lenient().when(mockLocation2.getFormattedLocation()).thenReturn("Philadelphia, USA");

        lenient().when(mockLocation3.getTimestamp()).thenReturn(baseTime.plusHours(4));
        lenient().when(mockLocation3.getFormattedLocation()).thenReturn("Washington DC, USA");
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create empty location history")
        void shouldCreateEmptyLocationHistory() {
            // Arrange - locationHistory already created in @BeforeEach

            // Act & Assert
            assertThat(locationHistory.getLocations()).isEmpty();
            assertThat(locationHistory.size()).isZero();
            assertThat(locationHistory.isEmpty()).isTrue();
            assertThat(locationHistory.getCurrentLocation()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Add Location Tests")
    class AddLocationTests {

        @Test
        @DisplayName("Should add location to empty history")
        void shouldAddLocationToEmptyHistory() {
            // Arrange - empty locationHistory and mockLocation1 ready

            // Act
            locationHistory.addLocation(mockLocation1);

            // Assert
            assertThat(locationHistory.size()).isEqualTo(1);
            assertThat(locationHistory.isEmpty()).isFalse();
            assertThat(locationHistory.getLocations()).containsExactly(mockLocation1);
            assertThat(locationHistory.getCurrentLocation()).contains(mockLocation1);
        }

        @Test
        @DisplayName("Should add multiple locations in chronological order")
        void shouldAddMultipleLocationsInChronologicalOrder() {
            // Arrange - locations with chronological timestamps ready

            // Act
            locationHistory.addLocation(mockLocation1);
            locationHistory.addLocation(mockLocation2);
            locationHistory.addLocation(mockLocation3);

            // Assert
            assertThat(locationHistory.size()).isEqualTo(3);
            assertThat(locationHistory.getLocations()).containsExactly(mockLocation1, mockLocation2, mockLocation3);
            assertThat(locationHistory.getCurrentLocation()).contains(mockLocation3);
        }

        @Test
        @DisplayName("Should throw exception when adding null location")
        void shouldThrowExceptionWhenAddingNullLocation() {
            // Arrange - locationHistory ready

            // Act & Assert
            assertThatThrownBy(() -> locationHistory.addLocation(null))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("Location cannot be null");

            assertThat(locationHistory.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when adding location with earlier timestamp")
        void shouldThrowExceptionWhenAddingLocationWithEarlierTimestamp() {
            // Arrange
            Location earlierLocation = mock(Location.class);
            when(earlierLocation.getTimestamp()).thenReturn(baseTime.minusHours(1));

            locationHistory.addLocation(mockLocation1);

            // Act & Assert
            assertThatThrownBy(() -> locationHistory.addLocation(earlierLocation))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("New location timestamp must be after the last location");

            assertThat(locationHistory.size()).isEqualTo(1);
            verify(earlierLocation).getTimestamp();
        }

        @Test
        @DisplayName("Should allow adding location with same timestamp")
        void shouldAllowAddingLocationWithSameTimestamp() {
            // Arrange
            Location sameTimeLocation = mock(Location.class);
            when(sameTimeLocation.getTimestamp()).thenReturn(baseTime);

            locationHistory.addLocation(mockLocation1);

            // Act
            locationHistory.addLocation(sameTimeLocation);

            // Assert
            assertThat(locationHistory.size()).isEqualTo(2);
            assertThat(locationHistory.getLocations()).containsExactly(mockLocation1, sameTimeLocation);
        }

        @Test
        @DisplayName("Should allow adding location with later timestamp")
        void shouldAllowAddingLocationWithLaterTimestamp() {
            // Arrange
            Location laterLocation = mock(Location.class);
            when(laterLocation.getTimestamp()).thenReturn(baseTime.plusMinutes(1));

            locationHistory.addLocation(mockLocation1);

            // Act
            locationHistory.addLocation(laterLocation);

            // Assert
            assertThat(locationHistory.size()).isEqualTo(2);
            assertThat(locationHistory.getCurrentLocation()).contains(laterLocation);
        }
    }

    @Nested
    @DisplayName("Get Current Location Tests")
    class GetCurrentLocationTests {

        @Test
        @DisplayName("Should return empty for empty history")
        void shouldReturnEmptyForEmptyHistory() {
            // Arrange - empty locationHistory

            // Act
            Optional<Location> result = locationHistory.getCurrentLocation();

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return last added location")
        void shouldReturnLastAddedLocation() {
            // Arrange
            locationHistory.addLocation(mockLocation1);
            locationHistory.addLocation(mockLocation2);

            // Act
            Optional<Location> result = locationHistory.getCurrentLocation();

            // Assert
            assertThat(result).contains(mockLocation2);
        }

        @Test
        @DisplayName("Should update current location when new location is added")
        void shouldUpdateCurrentLocationWhenNewLocationIsAdded() {
            // Arrange
            locationHistory.addLocation(mockLocation1);
            assertThat(locationHistory.getCurrentLocation()).contains(mockLocation1);

            // Act
            locationHistory.addLocation(mockLocation2);

            // Assert
            assertThat(locationHistory.getCurrentLocation()).contains(mockLocation2);
        }
    }

    @Nested
    @DisplayName("Get Location At Index Tests")
    class GetLocationAtIndexTests {

        @BeforeEach
        void setUpLocations() {
            locationHistory.addLocation(mockLocation1);
            locationHistory.addLocation(mockLocation2);
            locationHistory.addLocation(mockLocation3);
        }

        @Test
        @DisplayName("Should return location at valid index")
        void shouldReturnLocationAtValidIndex() {
            // Arrange - locations already added in @BeforeEach

            // Act & Assert
            assertThat(locationHistory.getLocationAt(0)).contains(mockLocation1);
            assertThat(locationHistory.getLocationAt(1)).contains(mockLocation2);
            assertThat(locationHistory.getLocationAt(2)).contains(mockLocation3);
        }

        @Test
        @DisplayName("Should return empty for negative index")
        void shouldReturnEmptyForNegativeIndex() {
            // Arrange - locations already added

            // Act
            Optional<Location> result = locationHistory.getLocationAt(-1);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty for index out of bounds")
        void shouldReturnEmptyForIndexOutOfBounds() {
            // Arrange - locations already added (size = 3)

            // Act
            Optional<Location> result = locationHistory.getLocationAt(3);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty for empty history")
        void shouldReturnEmptyForEmptyHistoryAtAnyIndex() {
            // Arrange - empty locationHistory
            LocationHistory emptyHistory = new LocationHistory();

            // Act & Assert
            assertThat(emptyHistory.getLocationAt(0)).isEmpty();
            assertThat(emptyHistory.getLocationAt(1)).isEmpty();
            assertThat(emptyHistory.getLocationAt(-1)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Locations Between Tests")
    class GetLocationsBetweenTests {

        @BeforeEach
        void setUpLocations() {
            locationHistory.addLocation(mockLocation1); // baseTime
            locationHistory.addLocation(mockLocation2); // baseTime + 2h
            locationHistory.addLocation(mockLocation3); // baseTime + 4h
        }

        @Test
        @DisplayName("Should return locations within time range")
        void shouldReturnLocationsWithinTimeRange() {
            // Arrange
            LocalDateTime start = baseTime.minusMinutes(30);
            LocalDateTime end = baseTime.plusHours(3);

            // Act
            List<Location> result = locationHistory.getLocationsBetween(start, end);

            // Assert
            assertThat(result).containsExactly(mockLocation1, mockLocation2);
        }

        @Test
        @DisplayName("Should return all locations when range covers all")
        void shouldReturnAllLocationsWhenRangeCoversAll() {
            // Arrange
            LocalDateTime start = baseTime.minusHours(1);
            LocalDateTime end = baseTime.plusHours(5);

            // Act
            List<Location> result = locationHistory.getLocationsBetween(start, end);

            // Assert
            assertThat(result).containsExactly(mockLocation1, mockLocation2, mockLocation3);
        }

        @Test
        @DisplayName("Should return empty list when no locations in range")
        void shouldReturnEmptyListWhenNoLocationsInRange() {
            // Arrange
            LocalDateTime start = baseTime.minusHours(2);
            LocalDateTime end = baseTime.minusHours(1);

            // Act
            List<Location> result = locationHistory.getLocationsBetween(start, end);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should include locations at exact start and end times")
        void shouldIncludeLocationsAtExactStartAndEndTimes() {
            // Arrange
            LocalDateTime start = baseTime; // Exact time of mockLocation1
            LocalDateTime end = baseTime.plusHours(4); // Exact time of mockLocation3

            // Act
            List<Location> result = locationHistory.getLocationsBetween(start, end);

            // Assert
            assertThat(result).containsExactly(mockLocation1, mockLocation2, mockLocation3);
        }

        @Test
        @DisplayName("Should return empty list for empty history")
        void shouldReturnEmptyListForEmptyHistory() {
            // Arrange
            LocationHistory emptyHistory = new LocationHistory();
            LocalDateTime start = baseTime;
            LocalDateTime end = baseTime.plusHours(1);

            // Act
            List<Location> result = emptyHistory.getLocationsBetween(start, end);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should return unmodifiable list from getLocations")
        void shouldReturnUnmodifiableListFromGetLocations() {
            // Arrange
            locationHistory.addLocation(mockLocation1);

            // Act
            List<Location> locations = locationHistory.getLocations();

            // Assert & Act & Assert
            assertThatThrownBy(() -> locations.add(mockLocation2))
                    .isInstanceOf(UnsupportedOperationException.class);

            assertThatThrownBy(() -> locations.remove(0))
                    .isInstanceOf(UnsupportedOperationException.class);

            assertThatThrownBy(() -> locations.clear())
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Copy Tests")
    class CopyTests {

        @Test
        @DisplayName("Should create independent copy")
        void shouldCreateIndependentCopy() {
            // Arrange
            locationHistory.addLocation(mockLocation1);
            locationHistory.addLocation(mockLocation2);

            // Act
            LocationHistory copy = locationHistory.copy();

            // Assert
            assertThat(copy.getLocations()).isEqualTo(locationHistory.getLocations());
            assertThat(copy.size()).isEqualTo(locationHistory.size());
            assertThat(copy).isNotSameAs(locationHistory);
        }

        @Test
        @DisplayName("Should not affect original when modifying copy")
        void shouldNotAffectOriginalWhenModifyingCopy() {
            // Arrange
            locationHistory.addLocation(mockLocation1);
            LocationHistory copy = locationHistory.copy();

            // Act
            copy.addLocation(mockLocation2);

            // Assert
            assertThat(locationHistory.size()).isEqualTo(1);
            assertThat(copy.size()).isEqualTo(2);
            assertThat(locationHistory.getLocations()).containsExactly(mockLocation1);
            assertThat(copy.getLocations()).containsExactly(mockLocation1, mockLocation2);
        }

        @Test
        @DisplayName("Should create empty copy for empty history")
        void shouldCreateEmptyCopyForEmptyHistory() {
            // Arrange - empty locationHistory

            // Act
            LocationHistory copy = locationHistory.copy();

            // Assert
            assertThat(copy.isEmpty()).isTrue();
            assertThat(copy.size()).isZero();
            assertThat(copy).isNotSameAs(locationHistory);
        }
    }

    @Nested
    @DisplayName("Size and Empty Tests")
    class SizeAndEmptyTests {

        @Test
        @DisplayName("Should return correct size")
        void shouldReturnCorrectSize() {
            // Arrange & Act & Assert
            assertThat(locationHistory.size()).isZero();
            assertThat(locationHistory.isEmpty()).isTrue();

            locationHistory.addLocation(mockLocation1);
            assertThat(locationHistory.size()).isEqualTo(1);
            assertThat(locationHistory.isEmpty()).isFalse();

            locationHistory.addLocation(mockLocation2);
            assertThat(locationHistory.size()).isEqualTo(2);
            assertThat(locationHistory.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return empty string for empty history")
        void shouldReturnEmptyStringForEmptyHistory() {
            // Arrange - empty locationHistory

            // Act
            String result = locationHistory.toString();

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return single location for single location history")
        void shouldReturnSingleLocationForSingleLocationHistory() {
            // Arrange
            locationHistory.addLocation(mockLocation1);

            // Act
            String result = locationHistory.toString();

            // Assert
            assertThat(result).isEqualTo("New York, USA");
            verify(mockLocation1).getFormattedLocation();
        }

        @Test
        @DisplayName("Should return formatted route for multiple locations")
        void shouldReturnFormattedRouteForMultipleLocations() {
            // Arrange
            locationHistory.addLocation(mockLocation1);
            locationHistory.addLocation(mockLocation2);
            locationHistory.addLocation(mockLocation3);

            // Act
            String result = locationHistory.toString();

            // Assert
            assertThat(result).isEqualTo("New York, USA -> Philadelphia, USA -> Washington DC, USA");
            verify(mockLocation1).getFormattedLocation();
            verify(mockLocation2).getFormattedLocation();
            verify(mockLocation3).getFormattedLocation();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle locations with identical timestamps")
        void shouldHandleLocationsWithIdenticalTimestamps() {
            // Arrange
            Location sameTimeLocation1 = mock(Location.class);
            Location sameTimeLocation2 = mock(Location.class);
            when(sameTimeLocation1.getTimestamp()).thenReturn(baseTime);
            when(sameTimeLocation2.getTimestamp()).thenReturn(baseTime);

            // Act
            locationHistory.addLocation(sameTimeLocation1);
            locationHistory.addLocation(sameTimeLocation2);

            // Assert
            assertThat(locationHistory.size()).isEqualTo(2);
            assertThat(locationHistory.getCurrentLocation()).contains(sameTimeLocation2);
        }

        @Test
        @DisplayName("Should maintain chronological order validation across multiple adds")
        void shouldMaintainChronologicalOrderValidationAcrossMultipleAdds() {
            // Arrange
            Location futureLocation = mock(Location.class);
            Location pastLocation = mock(Location.class);
            when(futureLocation.getTimestamp()).thenReturn(baseTime.plusHours(1));
            when(pastLocation.getTimestamp()).thenReturn(baseTime.minusHours(1));

            locationHistory.addLocation(mockLocation1); // baseTime
            locationHistory.addLocation(futureLocation); // baseTime + 1h

            // Act & Assert
            assertThatThrownBy(() -> locationHistory.addLocation(pastLocation))
                    .isInstanceOf(InvalidPackageDataException.class)
                    .hasMessage("New location timestamp must be after the last location");

            assertThat(locationHistory.size()).isEqualTo(2);
        }
    }
}
