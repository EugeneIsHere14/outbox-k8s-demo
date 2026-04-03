package com.demo.payment.dto;

import com.demo.payment.enums.PaymentStatus;

import java.math.BigDecimal;

public record PaymentDto(Long id, Long orderId, BigDecimal amount, PaymentStatus status) {
}
