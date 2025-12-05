package com.logitrack.domain.event;
import com.logitrack.domain.model.PackageStatus;
import com.logitrack.domain.model.Recipient;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PackageCreatedEvent  extends BaseDomainEvent{
    private final String packageId;
    private final Recipient recipient;
    private final double weight;
    private final PackageStatus status;

    @Builder
    public PackageCreatedEvent(String packageId, Recipient recipient,
                               double weight, PackageStatus status,
                               LocalDateTime occurredAt) {
        super(packageId, occurredAt);
        this.packageId = packageId;
        this.recipient = recipient;
        this.weight = weight;
        this.status = status;
    }

    @Override
    public String getEventType() {
        return "PACKAGE_CREATED";
    }
}
