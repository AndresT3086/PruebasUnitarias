package com.logitrack.domain.event;

import com.logitrack.domain.model.Location;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LocationAddedEvent extends BaseDomainEvent {
    private final String packageId;
    private final Location location;

    @Builder
    public LocationAddedEvent(String packageId, Location location, LocalDateTime occurredAt) {
        super(packageId, occurredAt);
        this.packageId = packageId;
        this.location = location;
    }

    @Override
    public String getEventType() {
        return "LOCATION_ADDED";
    }
}
