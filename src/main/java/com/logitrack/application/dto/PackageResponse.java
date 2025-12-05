package com.logitrack.application.dto;

import com.logitrack.domain.model.PackageStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PackageResponse {
    private String id;
    private RecipientDto recipient;
    private DimensionsDto dimensions;
    private double weight;
    private PackageStatus status;
    private List<LocationDto> locations;
    private String currentLocation;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class RecipientDto {
        private String name;
        private String email;
        private String phone;
        private AddressDto address;
    }

    @Data
    @Builder
    public static class AddressDto {
        private String street;
        private String city;
        private String state;
        private String country;
        private String postalCode;
        private String fullAddress;
    }

    @Data
    @Builder
    public static class DimensionsDto {
        private double height;
        private double width;
        private double depth;
        private double volume;
    }

    @Data
    @Builder
    public static class LocationDto {
        private String city;
        private String country;
        private String description;
        private LocalDateTime timestamp;
        private Double latitude;
        private Double longitude;
    }
}
