package com.logitrack.domain.port.out;

import java.util.Optional;

public interface LocationService {
    Optional<LocationInfo> getLocationInfo(String city, String country);
    Optional<LocationInfo> getLocationByCoordinates(double latitude, double longitude);
    boolean validateLocation(String city, String country);

    record LocationInfo(
            String city,
            String country,
            String state,
            Double latitude,
            Double longitude,
            String timezone
    ) {}
}
