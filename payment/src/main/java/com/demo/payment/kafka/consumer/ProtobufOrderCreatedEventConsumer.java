package com.demo.payment.kafka.consumer;

import com.demo.payment.enums.OrderEventType;
import com.demo.payment.mapper.ProtobufOrderStatusMapper;
import com.demo.protobuf.order.event.OrderEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnBooleanProperty(name = "app.kafka.enabled")
@AllArgsConstructor
public class ProtobufOrderCreatedEventConsumer {

    private final ProtobufOrderStatusMapper statusMapper;

    @KafkaListener(
            topics = "order-events-protobuf",
            groupId = "protobuf-payment-group",
            containerFactory = "protobufKafkaListenerContainerFactory"
    )
    public void consume(final OrderEvent event) {
        if (event == null) {
            log.warn("Received null protobuf order event");
            return;
        }

        log.info("Received protobuf order event. eventType={}, orderId={}, customerName={}, amount={}, rawstatus={}, " +
                        "mappedStatus={}",
                event.getEventType(),
                event.getOrderId(),
                event.getCustomerName(),
                event.getAmount(),
                event.getStatus(),
                statusMapper.mapStatus(event.getStatus()));

        if (!OrderEventType.ORDER_CREATED.name().equals(event.getEventType())) {
            log.info("Skipping unsupported protobuf event type={}", event.getEventType());
            return;
        }

        log.info("Starting protobuf payment flow for orderId={}", event.getOrderId());
    }
}
