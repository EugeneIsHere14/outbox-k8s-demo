package com.demo.payment.mapper;

import com.demo.payment.enums.OrderStatus;
import org.springframework.stereotype.Component;

@Component
public class ProtobufOrderStatusMapper {

    public OrderStatus mapStatus(final com.demo.protobuf.order.enums.OrderStatus status) {
        return switch (status) {
            case ORDER_STATUS_CREATED -> OrderStatus.CREATED;
            case ORDER_STATUS_PAID -> OrderStatus.PAID;
            case ORDER_STATUS_REJECTED -> OrderStatus.REJECTED;
            case ORDER_STATUS_SHIPPED -> OrderStatus.SHIPPED;
            case ORDER_STATUS_DELIVERED -> OrderStatus.DELIVERED;
            default -> OrderStatus.UNKNOWN;
        };
    }
}
