package com.logitrack.domain.event;

import com.logitrack.domain.model.PackageStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PackageStatusChangedEvent extends BaseDomainEvent {
    private final String packageId;
    private final PackageStatus previousStatus;
    private final PackageStatus newStatus;

    @Builder
    public PackageStatusChangedEvent(String packageId, PackageStatus previousStatus,
                                     PackageStatus newStatus, LocalDateTime occurredAt) {
        super(packageId, occurredAt);
        this.packageId = packageId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }

    @Override
    public String getEventType() {
        return "PACKAGE_STATUS_CHANGED";
    }
}
