package com.logitrack.infrastructure.adapter.out.external;

import com.logitrack.domain.port.out.LocationService;
import com.logitrack.infrastructure.adapter.out.external.client.GeocodeApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationServiceAdapter implements LocationService{

    private final GeocodeApiClient geocodeClient;

    @Override
    @Cacheable(value = "locations", key = "#city + '_' + #country")
    public Optional<LocationInfo> getLocationInfo(String city, String country) {
        log.debug("Getting location info for: {}, {}", city, country);

        return geocodeClient.geocodeLocation(city, country)
                .map(response -> {
                    var result = response.getResults().get(0);
                    var components = result.getComponents();
                    var geometry = result.getGeometry();

                    String resolvedCity = components.getCity() != null ? components.getCity() :
                            components.getTown() != null ? components.getTown() :
                                    components.getVillage() != null ? components.getVillage() : city;

                    return new LocationInfo(
                            resolvedCity,
                            components.getCountry() != null ? components.getCountry() : country,
                            components.getState(),
                            geometry.getLat(),
                            geometry.getLng(),
                            null // Timezone can be added if needed
                    );
                });
    }

    @Override
    @Cacheable(value = "coordinates", key = "#latitude + '_' + #longitude")
    public Optional<LocationInfo> getLocationByCoordinates(double latitude, double longitude) {
        log.debug("Getting location for coordinates: {}, {}", latitude, longitude);

        return geocodeClient.reverseGeocode(latitude, longitude)
                .map(response -> {
                    var result = response.getResults().get(0);
                    var components = result.getComponents();

                    String city = components.getCity() != null ? components.getCity() :
                            components.getTown() != null ? components.getTown() :
                                    components.getVillage() != null ? components.getVillage() : "Unknown";

                    return new LocationInfo(
                            city,
                            components.getCountry() != null ? components.getCountry() : "Unknown",
                            components.getState(),
                            latitude,
                            longitude,
                            null
                    );
                });
    }

    @Override
    public boolean validateLocation(String city, String country) {
        log.debug("Validating location: {}, {}", city, country);

        // Simple validation - check if location can be geocoded
        return getLocationInfo(city, country).isPresent();
    }
}
