package com.logitrack.domain.model;

import lombok.Getter;

@Getter
public enum PackageStatus {
    CREATED("Created", true),
    IN_TRANSIT("In Transit", true),
    OUT_FOR_DELIVERY("Out for Delivery", true),
    DELIVERED("Delivered", false),
    DELIVERY_FAILED("Delivery Failed", true),
    RETURNED("Returned", false);

    private final String description;
    private final boolean canTransition;

    PackageStatus(String description, boolean canTransition) {
        this.description = description;
        this.canTransition = canTransition;
    }

    public boolean canTransition() {
        return canTransition;
    }
}
