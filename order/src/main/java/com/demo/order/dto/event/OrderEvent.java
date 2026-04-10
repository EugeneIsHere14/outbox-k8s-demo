package com.demo.order.dto.event;

import com.demo.order.enums.OrderEventType;
import com.demo.order.enums.OrderStatus;

import java.math.BigDecimal;

public record OrderEvent(OrderEventType eventType,
                         Long orderId,
                         String customerName,
                         BigDecimal amount,
                         OrderStatus status) {
}
