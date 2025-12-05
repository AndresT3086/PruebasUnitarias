package com.logitrack.domain.event;

import java.time.LocalDateTime;

public interface DomainEvent {
    String getEventId();
    String getAggregateId();
    LocalDateTime getOccurredAt();
    String getEventType();
}
