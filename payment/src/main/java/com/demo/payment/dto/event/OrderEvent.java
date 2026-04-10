package com.demo.payment.dto.event;

import com.demo.payment.enums.OrderEventType;
import com.demo.payment.enums.OrderStatus;

import java.math.BigDecimal;

public record OrderEvent(OrderEventType eventType,
                         Long orderId,
                         String customerName,
                         BigDecimal amount,
                         OrderStatus status) {
}
