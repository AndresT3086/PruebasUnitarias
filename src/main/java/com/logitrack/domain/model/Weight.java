package com.logitrack.domain.model;

import com.logitrack.domain.exception.InvalidPackageDataException;
import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Value
public class Weight {
    BigDecimal valueInKg;

    private Weight(BigDecimal valueInKg) {
        if (valueInKg == null || valueInKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPackageDataException("Weight must be positive");
        }
        if (valueInKg.compareTo(new BigDecimal("1000")) > 0) {
            throw new InvalidPackageDataException("Weight cannot exceed 1000 kg");
        }
        this.valueInKg = valueInKg.setScale(3, RoundingMode.HALF_UP);
    }

    public static Weight ofKilograms(double kg) {
        return new Weight(BigDecimal.valueOf(kg));
    }

    public static Weight ofGrams(double grams) {
        return new Weight(BigDecimal.valueOf(grams / 1000));
    }

    public double toKilograms() {
        return valueInKg.doubleValue();
    }

    public double toGrams() {
        return valueInKg.multiply(BigDecimal.valueOf(1000)).doubleValue();
    }
}
