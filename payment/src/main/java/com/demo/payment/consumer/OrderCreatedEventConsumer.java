package com.demo.payment.consumer;

import com.demo.payment.dto.event.OrderEvent;
import com.demo.payment.enums.OrderEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@ConditionalOnBooleanProperty(name = "app.kafka.enabled")
@RequiredArgsConstructor
public class OrderCreatedEventConsumer {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-events", groupId = "payment-group")
    public void consume(final String message) {
        try {
            JsonNode root = objectMapper.readTree(message);

            JsonNode eventNode = root.hasNonNull("payload") && root.get("payload").isObject()
                    ? root.get("payload")
                    : root;

            OrderEvent event = objectMapper.treeToValue(eventNode, OrderEvent.class);

            if (event == null) {
                log.warn("Received null order event. Raw message={}", message);
                return;
            }

            log.info("Received order event. Type: {}, orderId: {}, customerName: {}",
                    event.eventType(), event.orderId(), event.customerName());

            if (event.eventType() != OrderEventType.ORDER_CREATED) {
                log.info("Skipping unsupported event type: {}", event.eventType());
                return;
            }

            log.info("Starting payment flow for orderId={}", event.orderId());
        } catch (Exception ex) {
            log.error("Failed to parse Kafka message. Raw message={}", message, ex);
        }
    }
}