package com.logitrack.domain.port.out;

import com.logitrack.domain.event.DomainEvent;

public interface EventPublisher {

    void publish(DomainEvent event);
    void publish(String topic, DomainEvent event);
}
