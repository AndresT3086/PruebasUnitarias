package com.logitrack.domain.model;

import com.logitrack.domain.exception.InvalidPackageDataException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class LocationHistory {
    private final List<Location> locations;

    public LocationHistory() {
        this.locations = new ArrayList<>();
    }

    private LocationHistory(List<Location> locations) {
        this.locations = new ArrayList<>(locations);
    }

    public void addLocation(Location location) {
        if (location == null) {
            throw new InvalidPackageDataException("Location cannot be null");
        }

        // Validate chronological order
        if (!locations.isEmpty()) {
            Location lastLocation = locations.get(locations.size() - 1);
            if (location.getTimestamp().isBefore(lastLocation.getTimestamp())) {
                throw new InvalidPackageDataException(
                        "New location timestamp must be after the last location"
                );
            }
        }
        locations.add(location);
    }

    public List<Location> getLocations() {
        return Collections.unmodifiableList(locations);
    }

    public Optional<Location> getCurrentLocation() {
        return locations.isEmpty() ? Optional.empty() :
                Optional.of(locations.get(locations.size() - 1));
    }

    public Optional<Location> getLocationAt(int index) {
        if (index < 0 || index >= locations.size()) {
            return Optional.empty();
        }
        return Optional.of(locations.get(index));
    }

    public List<Location> getLocationsBetween(LocalDateTime start, LocalDateTime end) {
        return locations.stream()
                .filter(loc -> !loc.getTimestamp().isBefore(start) &&
                        !loc.getTimestamp().isAfter(end))
                .toList();
    }

    public int size() {
        return locations.size();
    }

    public boolean isEmpty() {
        return locations.isEmpty();
    }

    public LocationHistory copy() {
        return new LocationHistory(this.locations);
    }

    @Override
    public String toString() {
        return locations.stream()
                .map(Location::getFormattedLocation)
                .collect(Collectors.joining(" -> "));
    }
}
