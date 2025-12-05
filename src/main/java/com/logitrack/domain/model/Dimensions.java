package com.logitrack.domain.model;

import com.logitrack.domain.exception.InvalidPackageDataException;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Value
@Builder
public class Dimensions {
    BigDecimal height;
    BigDecimal width;
    BigDecimal depth;

    public Dimensions(BigDecimal height, BigDecimal width, BigDecimal depth) {
        validate(height, "Height");
        validate(width, "Width");
        validate(depth, "Depth");

        this.height = height.setScale(2, RoundingMode.HALF_UP);
        this.width = width.setScale(2, RoundingMode.HALF_UP);
        this.depth = depth.setScale(2, RoundingMode.HALF_UP);
    }

    private void validate(BigDecimal value, String field) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPackageDataException(field + " must be positive");
        }
        if (value.compareTo(new BigDecimal("500")) > 0) {
            throw new InvalidPackageDataException(field + " cannot exceed 500 cm");
        }
    }

    public BigDecimal calculateVolume() {
        return height.multiply(width).multiply(depth)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static Dimensions of(double height, double width, double depth) {
        return new Dimensions(
                BigDecimal.valueOf(height),
                BigDecimal.valueOf(width),
                BigDecimal.valueOf(depth)
        );
    }
}
