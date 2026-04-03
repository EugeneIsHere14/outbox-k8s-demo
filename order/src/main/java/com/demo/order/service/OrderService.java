package com.demo.order.service;

import com.demo.order.dto.OrderDto;
import com.demo.order.entity.Order;
import com.demo.order.entity.OutboxEvent;
import com.demo.order.enums.AggregateType;
import com.demo.order.enums.OrderEventType;
import com.demo.order.enums.OrderStatus;
import com.demo.order.repository.OrderRepository;
import com.demo.order.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;

    public OrderService(final OrderRepository orderRepository, final OutboxEventRepository outboxEventRepository) {
        this.orderRepository = orderRepository;
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional
    public OrderDto createOrder(OrderDto orderDto) {
        Order order = new Order();
        order.setCustomerName(orderDto.customerName());
        order.setAmount(orderDto.amount());
        order.setStatus(OrderStatus.CREATED);
        Order saved = orderRepository.save(order);

        saveOutboxEvent(saved, OrderEventType.ORDER_CREATED);

        return new OrderDto(saved.getId(), orderDto.customerName(), orderDto.amount(), saved.getStatus());
    }

    public Optional<OrderDto> getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(order -> new OrderDto(order.getId(), order.getCustomerName(), order.getAmount(), order.getStatus()));
    }

    private void saveOutboxEvent(Order order, OrderEventType eventType) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType(AggregateType.ORDER);
        event.setAggregateId(order.getId());
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(eventType);
        event.setPayload(String.format("{\"orderId\":%d,\"customerName\":\"%s\",\"amount\":%s,\"status\":\"%s\"}",
                order.getId(), order.getCustomerName(), order.getAmount(), order.getStatus()));
        event.setCreatedAt(LocalDateTime.now());

        outboxEventRepository.save(event);
    }
}
