package com.logitrack.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class Location {
    String id;
    String city;
    String country;
    String description;
    LocalDateTime timestamp;
    Double latitude;
    Double longitude;

    public static Location create(String city, String country, String description,
                                  Double latitude, Double longitude) {
        return Location.builder()
                .id(UUID.randomUUID().toString())
                .city(city)
                .country(country)
                .description(description)
                .timestamp(LocalDateTime.now())
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }

    public String getFormattedLocation() {
        return String.format("%s, %s", city, country);
    }
}
