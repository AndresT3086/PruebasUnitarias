package com.logitrack.infrastructure.adapter.out.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logitrack.domain.event.DomainEvent;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaEventPublisher Tests")
class KafkaEventPublisherTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private DomainEvent domainEvent;

    @InjectMocks
    private KafkaEventPublisher kafkaEventPublisher;

    @Nested
    @DisplayName("Publish Event with Default Topic Tests")
    class PublishEventWithDefaultTopicTests {

        @Test
        @DisplayName("Should publish event to default topic successfully")
        void shouldPublishEventToDefaultTopicSuccessfully() throws Exception {
            // Arrange
            String testEventJson = "{\"eventType\":\"TEST_EVENT\"}";
            String testAggregateId = "PKG-12345";

            when(domainEvent.getAggregateId()).thenReturn(testAggregateId);
            when(objectMapper.writeValueAsString(domainEvent)).thenReturn(testEventJson);
            when(kafkaTemplate.send(eq("package-events"), eq(testAggregateId), eq(testEventJson)))
                    .thenReturn(mock(CompletableFuture.class));

            // Act
            kafkaEventPublisher.publish(domainEvent);

            // Assert
            verify(objectMapper).writeValueAsString(domainEvent);
            verify(kafkaTemplate).send("package-events", testAggregateId, testEventJson);
        }
    }

    @Nested
    @DisplayName("Publish Event with Custom Topic Tests")
    class PublishEventWithCustomTopicTests {

        @Test
        @DisplayName("Should publish event to custom topic successfully")
        void shouldPublishEventToCustomTopicSuccessfully() throws Exception {
            // Arrange
            String customTopic = "custom-events";
            String testEventJson = "{\"eventType\":\"CUSTOM_EVENT\"}";
            String testAggregateId = "PKG-CUSTOM";

            when(domainEvent.getAggregateId()).thenReturn(testAggregateId);
            when(objectMapper.writeValueAsString(domainEvent)).thenReturn(testEventJson);
            when(kafkaTemplate.send(eq(customTopic), eq(testAggregateId), eq(testEventJson)))
                    .thenReturn(mock(CompletableFuture.class));

            // Act
            kafkaEventPublisher.publish(customTopic, domainEvent);

            // Assert
            verify(objectMapper).writeValueAsString(domainEvent);
            verify(kafkaTemplate).send(customTopic, testAggregateId, testEventJson);
        }
    }

    @Nested
    @DisplayName("Serialization Error Tests")
    class SerializationErrorTests {

        @Test
        @DisplayName("Should throw RuntimeException when JSON serialization fails")
        void shouldThrowRuntimeExceptionWhenJsonSerializationFails() throws Exception {
            // Arrange
            JsonProcessingException jsonException = new JsonProcessingException("Invalid JSON") {};
            when(objectMapper.writeValueAsString(domainEvent)).thenThrow(jsonException);
            when(domainEvent.getEventType()).thenReturn("SERIALIZATION_ERROR");

            // Act & Assert
            assertThatThrownBy(() -> kafkaEventPublisher.publish(domainEvent))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Failed to publish event")
                    .hasCause(jsonException);

            verify(objectMapper).writeValueAsString(domainEvent);
            verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Null Handling Tests")
    class NullHandlingTests {

        @Test
        @DisplayName("Should handle null aggregate ID gracefully")
        void shouldHandleNullAggregateIdGracefully() throws Exception {
            // Arrange
            String testEventJson = "{\"eventType\":\"NULL_ID_TEST\"}";
            when(domainEvent.getAggregateId()).thenReturn(null);
            when(objectMapper.writeValueAsString(domainEvent)).thenReturn(testEventJson);

            // FIX: Specific stubbing for null key
            when(kafkaTemplate.send(eq("package-events"), isNull(), eq(testEventJson)))
                    .thenReturn(mock(CompletableFuture.class));

            // Act
            kafkaEventPublisher.publish(domainEvent);

            // Assert
            verify(kafkaTemplate).send("package-events", null, testEventJson);
        }

        @Test
        @DisplayName("Should handle null topic")
        void shouldHandleNullTopic() throws Exception {
            // Arrange
            String testEventJson = "{\"eventType\":\"NULL_TOPIC_TEST\"}";
            String testAggregateId = "PKG-NULL-TOPIC";
            when(domainEvent.getAggregateId()).thenReturn(testAggregateId);
            when(objectMapper.writeValueAsString(domainEvent)).thenReturn(testEventJson);

            // FIX: Specific stubbing for null topic
            when(kafkaTemplate.send(isNull(), eq(testAggregateId), eq(testEventJson)))
                    .thenReturn(mock(CompletableFuture.class));

            // Act
            kafkaEventPublisher.publish(null, domainEvent);

            // Assert
            verify(kafkaTemplate).send(null, testAggregateId, testEventJson);
        }
    }

    @Nested
    @DisplayName("Integration Flow Tests")
    class IntegrationFlowTests {

        @Test
        @DisplayName("Should complete entire publish flow without errors")
        void shouldCompleteEntirePublishFlowWithoutErrors() throws Exception {
            // Arrange
            String testEventJson = "{\"eventType\":\"INTEGRATION_TEST\"}";
            String testAggregateId = "PKG-INTEGRATION";

            when(domainEvent.getAggregateId()).thenReturn(testAggregateId);
            when(objectMapper.writeValueAsString(domainEvent)).thenReturn(testEventJson);
            when(kafkaTemplate.send(eq("integration-topic"), eq(testAggregateId), eq(testEventJson)))
                    .thenReturn(mock(CompletableFuture.class));

            // Act
            assertThatCode(() -> kafkaEventPublisher.publish("integration-topic", domainEvent))
                    .doesNotThrowAnyException();

            // Assert
            verify(objectMapper).writeValueAsString(domainEvent);
            verify(kafkaTemplate).send("integration-topic", testAggregateId, testEventJson);
        }
    }
}
