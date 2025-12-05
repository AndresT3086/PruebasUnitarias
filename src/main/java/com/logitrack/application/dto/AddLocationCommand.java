package com.logitrack.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddLocationCommand {
    @NotNull(message = "Package ID is required")
    private String packageId;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Country is required")
    private String country;

    private String description;

    private Double latitude;
    private Double longitude;
}
