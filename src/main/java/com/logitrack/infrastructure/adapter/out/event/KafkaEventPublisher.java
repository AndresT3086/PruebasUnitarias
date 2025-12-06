package com.logitrack.infrastructure.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logitrack.domain.event.DomainEvent;
import com.logitrack.domain.port.out.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String DEFAULT_TOPIC = "package-events";

    @Override
    public void publish(DomainEvent event) {
        publish(DEFAULT_TOPIC, event);
    }

    @Override
    public void publish(String topic, DomainEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            String key = event.getAggregateId();

            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(topic, key, eventJson);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Event published successfully: type={}, id={}, topic={}, partition={}, offset={}",
                            event.getEventType(),
                            event.getEventId(),
                            topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                    );
                } else {
                    log.error("Failed to publish event: type={}, id={}, topic={}",
                            event.getEventType(),
                            event.getEventId(),
                            topic,
                            ex
                    );
                }
            });

        } catch (Exception e) {
            log.error("Error serializing event: {}", event.getEventType(), e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}
