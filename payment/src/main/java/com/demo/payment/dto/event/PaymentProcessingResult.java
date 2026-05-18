package com.demo.payment.dto.event;

import com.demo.payment.enums.PaymentEventType;
import com.demo.payment.enums.PaymentStatus;

import java.math.BigDecimal;

public record PaymentProcessingResult(
        String eventId,
        PaymentEventType eventType,
        Long orderId,
        String customerName,
        BigDecimal amount,
        PaymentStatus status
) {

    public static PaymentProcessingResult reserved(
            final String eventId,
            final Long orderId,
            final String customerName,
            final BigDecimal amount
    ) {
        return new PaymentProcessingResult(
                eventId,
                PaymentEventType.PAYMENT_RESERVED,
                orderId,
                customerName,
                amount,
                PaymentStatus.RESERVED
        );
    }

    public static PaymentProcessingResult alreadyProcessed(
            final String eventId,
            final Long orderId,
            final String customerName,
            final BigDecimal amount
    ) {
        return new PaymentProcessingResult(
                eventId,
                PaymentEventType.PAYMENT_ALREADY_PROCESSED,
                orderId,
                customerName,
                amount,
                PaymentStatus.ALREADY_PROCESSED
        );
    }

    public static PaymentProcessingResult rejected(
            final String eventId,
            final Long orderId,
            final String customerName,
            final BigDecimal amount
    ) {
        return new PaymentProcessingResult(
                eventId,
                PaymentEventType.PAYMENT_REJECTED,
                orderId,
                customerName,
                amount,
                PaymentStatus.REJECTED
        );
    }
}
