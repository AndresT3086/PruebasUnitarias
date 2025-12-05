package com.logitrack.domain.event;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@SuperBuilder
public abstract class BaseDomainEvent implements DomainEvent {
    private final String eventId;
    private final String aggregateId;
    private final LocalDateTime occurredAt;

    protected BaseDomainEvent(String aggregateId, LocalDateTime occurredAt) {
        this.eventId = UUID.randomUUID().toString();
        this.aggregateId = aggregateId;
        this.occurredAt = occurredAt != null ? occurredAt : LocalDateTime.now();
    }

}
