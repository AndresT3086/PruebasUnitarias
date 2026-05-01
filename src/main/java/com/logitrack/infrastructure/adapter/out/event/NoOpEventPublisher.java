package com.logitrack.infrastructure.adapter.out.event;

import com.logitrack.domain.event.DomainEvent;
import com.logitrack.domain.port.out.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpEventPublisher implements EventPublisher {

    @Override
    public void publish(DomainEvent event) {
        log.debug("Kafka disabled — event {} not published", event.getEventType());
    }

    @Override
    public void publish(String topic, DomainEvent event) {
        log.debug("Kafka disabled — event {} not published to topic {}", event.getEventType(), topic);
    }

}
