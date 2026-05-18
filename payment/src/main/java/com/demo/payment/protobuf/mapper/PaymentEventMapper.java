package com.demo.payment.protobuf.mapper;

import com.demo.payment.dto.event.PaymentProcessingResult;
import com.demo.payment.enums.PaymentStatus;
import com.demo.protobuf.payment.event.PaymentEvent;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventMapper {

    public PaymentEvent toProtobuf(final PaymentProcessingResult result) {
        return PaymentEvent.newBuilder()
                .setEventId(result.eventId())
                .setEventType(result.eventType().name())
                .setOrderId(result.orderId())
                .setCustomerName(result.customerName())
                .setAmount(result.amount().toString())
                .setStatus(mapStatus(result.status()))
                .build();
    }

    private com.demo.protobuf.payment.enums.PaymentStatus mapStatus(final PaymentStatus status) {
        return switch (status) {
            case PENDING -> com.demo.protobuf.payment.enums.PaymentStatus.PAYMENT_STATUS_PENDING;

            case RESERVED -> com.demo.protobuf.payment.enums.PaymentStatus.PAYMENT_STATUS_RESERVED;

            case PROCESSED -> com.demo.protobuf.payment.enums.PaymentStatus.PAYMENT_STATUS_PROCESSED;

            case ALREADY_PROCESSED -> com.demo.protobuf.payment.enums.PaymentStatus.PAYMENT_STATUS_ALREADY_PROCESSED;

            case COMPLETED -> com.demo.protobuf.payment.enums.PaymentStatus.PAYMENT_STATUS_COMPLETED;

            case REJECTED -> com.demo.protobuf.payment.enums.PaymentStatus.PAYMENT_STATUS_REJECTED;

            case UNKNOWN -> com.demo.protobuf.payment.enums.PaymentStatus.PAYMENT_STATUS_UNSPECIFIED;
        };
    }
}
