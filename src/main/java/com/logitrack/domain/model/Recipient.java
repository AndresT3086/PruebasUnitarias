package com.logitrack.domain.model;

import com.logitrack.domain.exception.InvalidPackageDataException;
import lombok.Builder;
import lombok.Value;

import java.util.regex.Pattern;

@Value
@Builder
public class Recipient {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[1-9]\\d{1,14}$");

    private static final String PHONE_CLEANUP_REGEX = "[\\s-]";

    String name;
    String email;
    String phone;
    Address address;

    public Recipient(String name, String email, String phone, Address address) {
        this.name = validateName(name);
        this.email = validateEmail(email);
        this.phone = validatePhone(phone);
        this.address = validateAddress(address);
    }

    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidPackageDataException("Recipient name is required");
        }
        if (name.length() > 100) {
            throw new InvalidPackageDataException("Recipient name too long");
        }
        return name.trim();
    }

    private String validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidPackageDataException("Invalid email format");
        }
        return email.toLowerCase();
    }

    private String validatePhone(String phone) {
        if (phone == null || !PHONE_PATTERN.matcher(phone.replaceAll(PHONE_CLEANUP_REGEX, "")).matches()) {
            throw new InvalidPackageDataException("Invalid phone number");
        }
        return phone.replaceAll(PHONE_CLEANUP_REGEX, "");
    }

    private Address validateAddress(Address address) {
        if (address == null) {
            throw new InvalidPackageDataException("Address is required");
        }
        return address;
    }

    @Value
    @Builder
    public static class Address {
        String street;
        String city;
        String state;
        String country;
        String postalCode;

        public Address(String street, String city, String state, String country, String postalCode) {
            this.street = validateField(street, "Street");
            this.city = validateField(city, "City");
            this.state = state; // Optional
            this.country = validateField(country, "Country");
            this.postalCode = validatePostalCode(postalCode);
        }

        private String validateField(String value, String fieldName) {
            if (value == null || value.trim().isEmpty()) {
                throw new InvalidPackageDataException(fieldName + " is required");
            }
            return value.trim();
        }

        private String validatePostalCode(String postalCode) {
            if (postalCode == null || postalCode.trim().isEmpty()) {
                throw new InvalidPackageDataException("Postal code is required");
            }
            String cleaned = postalCode.replaceAll(PHONE_CLEANUP_REGEX, "");
            if (cleaned.length() < 3 || cleaned.length() > 10) {
                throw new InvalidPackageDataException("Invalid postal code");
            }
            return cleaned;
        }

        public String getFullAddress() {
            StringBuilder sb = new StringBuilder();
            sb.append(street).append(", ");
            sb.append(city);
            if (state != null && !state.isEmpty()) {
                sb.append(", ").append(state);
            }
            sb.append(", ").append(country);
            sb.append(" ").append(postalCode);
            return sb.toString();
        }
    }
}
