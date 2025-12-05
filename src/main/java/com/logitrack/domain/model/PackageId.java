package com.logitrack.domain.model;

import com.logitrack.domain.exception.InvalidPackageDataException;
import lombok.Value;

import java.util.UUID;
import java.util.regex.Pattern;

@Value
public class PackageId {
    private static final String PREFIX = "LT-";
    private static final Pattern VALID_PATTERN = Pattern.compile("^LT-[A-Z0-9]{9}$");

    String value;

    private PackageId(String value) {
        if (value == null || !VALID_PATTERN.matcher(value).matches()) {
            throw new InvalidPackageDataException("Invalid package ID format: " + value);
        }
        this.value = value;
    }

    public static PackageId generate() {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 9).toUpperCase();
        return new PackageId(PREFIX + uuid);
    }

    public static PackageId of(String value) {
        return new PackageId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
