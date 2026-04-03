package com.demo.order.dto;

import com.demo.order.enums.OrderStatus;

import java.math.BigDecimal;

public record OrderDto(Long id, String customerName, BigDecimal amount, OrderStatus status) {
}
