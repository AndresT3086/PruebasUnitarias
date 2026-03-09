package com.logitrack.application.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePackageCommand {

    @NotBlank(message = "Recipient name is required")
    @Size(max = 100)
    private String recipientName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String recipientEmail;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number")
    private String recipientPhone;

    @NotBlank(message = "Street is required")
    private String street;

    @NotBlank(message = "City is required")
    private String city;

    private String state;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "Postal code is required")
    @Size(min = 3, max = 10)
    private String postalCode;

    @NotNull(message = "Height is required")
    @Positive(message = "Height must be positive")
    @Max(value = 500, message = "Height cannot exceed 500 cm")
    private Double height;

    @NotNull(message = "Width is required")
    @Positive(message = "Width must be positive")
    @Max(value = 500, message = "Width cannot exceed 500 cm")
    private Double width;

    @NotNull(message = "Depth is required")
    @Positive(message = "Depth must be positive")
    @Max(value = 500, message = "Depth cannot exceed 500 cm")
    private Double depth;

    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    @Max(value = 1000, message = "Weight cannot exceed 1000 kg")
    private Double weight;

    @Size(max = 500)
    private String notes;

}
