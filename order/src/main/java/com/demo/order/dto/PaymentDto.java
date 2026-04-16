package com.demo.order.dto;

import com.demo.order.enums.PaymentStatus;

import java.math.BigDecimal;

public record PaymentDto(Long id, Long orderId, BigDecimal amount, PaymentStatus status) {
}
