package com.logitrack.domain.model;

import com.logitrack.domain.event.*;
import com.logitrack.domain.exception.InvalidPackageDataException;
import com.logitrack.domain.exception.InvalidStateTransitionException;
import com.logitrack.domain.model.state.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.*;

@Getter
public class Package {

    private final PackageId id;
    private final Recipient recipient;
    private final Dimensions dimensions;
    private final Weight weight;
    private final LocationHistory locationHistory;

    @Setter(AccessLevel.PACKAGE)
    private PackageState state;

    @Setter(AccessLevel.PACKAGE)
    private LocalDateTime createdAt;

    @Setter(AccessLevel.PACKAGE)
    private LocalDateTime updatedAt;

    @Setter(AccessLevel.PACKAGE)
    private boolean deleted;

    private String notes;

    private final List<DomainEvent> domainEvents;

    private Package(Builder builder) {
        this.id = builder.id != null ? builder.id : PackageId.generate();
        this.recipient = validateNotNull(builder.recipient, "Recipient");
        this.dimensions = validateNotNull(builder.dimensions, "Dimensions");
        this.weight = validateNotNull(builder.weight, "Weight");
        this.locationHistory = new LocationHistory();
        this.state = new CreatedState();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.deleted = false;
        this.notes = builder.notes;
        this.domainEvents = new ArrayList<>();

        registerEvent(PackageCreatedEvent.builder()
                .packageId(this.id.getValue())
                .recipient(this.recipient)
                .weight(this.weight.toKilograms())
                .status(getStatus())
                .occurredAt(this.createdAt)
                .build());
    }

    private <T> T validateNotNull(T value, String fieldName) {
        if (value == null) {
            throw new InvalidPackageDataException(fieldName + " cannot be null");
        }
        return value;
    }

    public void changeStatus(PackageStatus newStatus) {
        PackageStatus currentStatus = getStatus();

        if (currentStatus == newStatus) {
            return;
        }

        if (!state.canTransitionTo(newStatus)) {
            throw new InvalidStateTransitionException(
                    String.format("Cannot transition from %s to %s", currentStatus, newStatus)
            );
        }

        switch (newStatus) {
            case IN_TRANSIT -> state.toInTransit(this);
            case OUT_FOR_DELIVERY -> state.toOutForDelivery(this);
            case DELIVERED -> state.toDelivered(this);
            case DELIVERY_FAILED -> state.toDeliveryFailed(this);
            case RETURNED -> state.toReturned(this);
            default -> throw new InvalidStateTransitionException("Unknown status: " + newStatus);
        }

        this.updatedAt = LocalDateTime.now();

        registerEvent(PackageStatusChangedEvent.builder()
                .packageId(this.id.getValue())
                .previousStatus(currentStatus)
                .newStatus(newStatus)
                .occurredAt(this.updatedAt)
                .build());
    }

    public void addLocation(Location location) {
        if (location == null) {
            throw new InvalidPackageDataException("Location cannot be null");
        }

        locationHistory.addLocation(location);
        this.updatedAt = LocalDateTime.now();

        registerEvent(LocationAddedEvent.builder()
                .packageId(this.id.getValue())
                .location(location)
                .occurredAt(LocalDateTime.now())
                .build());
    }

    public void softDelete() {
        this.deleted = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateNotes(String notes) {
        this.notes = notes;
        this.updatedAt = LocalDateTime.now();
    }

    public PackageStatus getStatus() {
        return state.getStatus();
    }

    public boolean isDelivered() {
        return getStatus() == PackageStatus.DELIVERED;
    }

    public boolean canBeModified() {
        return !deleted && getStatus().canTransition();
    }

    public Optional<Location> getCurrentLocation() {
        return locationHistory.getCurrentLocation();
    }

    private void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> getAndClearEvents() {
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private PackageId id;
        private Recipient recipient;
        private Dimensions dimensions;
        private Weight weight;
        private String notes;

        public Builder id(PackageId id) {
            this.id = id;
            return this;
        }

        public Builder recipient(Recipient recipient) {
            this.recipient = recipient;
            return this;
        }

        public Builder dimensions(Dimensions dimensions) {
            this.dimensions = dimensions;
            return this;
        }

        public Builder weight(Weight weight) {
            this.weight = weight;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public Package build() {
            return new Package(this);
        }
    }

    public void applyState(PackageState newState) {
        this.state = newState;
    }
}
