package com.logitrack.infrastructure.adapter.out.external;

import com.logitrack.domain.port.out.LocationService;
import com.logitrack.infrastructure.adapter.out.external.client.GeocodeApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationServiceAdapter implements LocationService {

    private final GeocodeApiClient geocodeClient;

    /**
     * Se inyecta a sí mismo (vía interfaz) para asegurar que las llamadas internas
     * pasen por el proxy de Spring y activen las anotaciones como @Cacheable.
     * Se usa @Lazy para evitar problemas de dependencia circular en algunas versiones de Spring.
     */
    @Lazy
    private final LocationService self;

    @Override
    @Cacheable(value = "locations", key = "#city + '_' + #country")
    public Optional<LocationInfo> getLocationInfo(String city, String country) {
        log.debug("Getting location info for: {}, {}", city, country);

        return geocodeClient.geocodeLocation(city, country)
                .map(response -> {
                    var result = response.getResults().get(0);
                    var components = result.getComponents();
                    var geometry = result.getGeometry();

                    return new LocationInfo(
                            resolveCity(components, city),
                            Optional.ofNullable(components.getCountry()).orElse(country),
                            components.getState(),
                            geometry.getLat(),
                            geometry.getLng(),
                            null
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

                    return new LocationInfo(
                            resolveCity(components, "Unknown"),
                            Optional.ofNullable(components.getCountry()).orElse("Unknown"),
                            components.getState(),
                            latitude,
                            longitude,
                            null
                    );
                });
    }

    private String resolveCity(GeocodeApiClient.GeocodeResponse.Components components, String fallback) {
        return Stream.of(components.getCity(), components.getTown(), components.getVillage())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(fallback);
    }

    @Override
    public boolean validateLocation(String city, String country) {
        log.debug("Validating location: {}, {}", city, country);

        // Usamos 'self' en lugar de llamar directamente al metodo para que la caché funcione
        return self.getLocationInfo(city, country).isPresent();
    }
}