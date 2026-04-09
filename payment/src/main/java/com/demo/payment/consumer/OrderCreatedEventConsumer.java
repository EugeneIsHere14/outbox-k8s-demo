package com.demo.payment.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedEventConsumer {

    @KafkaListener(topics = "order-events", groupId = "payment-group")
    public void consume(String message) {
        log.info("Received Kafka message: {}", message);

        // TODO: parse event and call payment service logic
    }
}