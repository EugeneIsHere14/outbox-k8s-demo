package com.demo.order.service;

import com.demo.order.client.PaymentServiceClient;
import com.demo.order.dto.OrderDto;
import com.demo.order.dto.PaymentDto;
import com.demo.order.dto.event.OrderEvent;
import com.demo.order.entity.Order;
import com.demo.order.entity.OutboxEvent;
import com.demo.order.entity.ProtobufOutboxEvent;
import com.demo.order.enums.*;
import com.demo.order.repository.OrderRepository;
import com.demo.order.repository.OutboxEventRepository;
import com.demo.order.repository.ProtobufOutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ProtobufOutboxEventRepository protobufOutboxEventRepository;
    private final PaymentServiceClient paymentServiceClient;
    private final ObjectMapper objectMapper;

    public OrderService(final OrderRepository orderRepository,
                        final OutboxEventRepository outboxEventRepository,
                        final ProtobufOutboxEventRepository protobufOutboxEventRepository,
                        final PaymentServiceClient paymentServiceClient,
                        final ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.protobufOutboxEventRepository = protobufOutboxEventRepository;
        this.paymentServiceClient = paymentServiceClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public OrderDto createOrder(OrderDto orderDto) {
        Order order = new Order();
        order.setCustomerName(orderDto.customerName());
        order.setAmount(orderDto.amount());
        order.setStatus(OrderStatus.CREATED);
        Order saved = orderRepository.save(order);

        saveOutboxEvent(saved, OrderEventType.ORDER_CREATED);

        return new OrderDto(saved.getId(), saved.getCustomerName(), saved.getAmount(), saved.getStatus());
    }

    @Transactional
    public OrderDto createOrderWithProtobuf(ProcessingFlow flow, OrderDto orderDto) {
        Order order = new Order();
        order.setCustomerName(orderDto.customerName());
        order.setAmount(orderDto.amount());
        order.setStatus(OrderStatus.CREATED);
        Order saved = orderRepository.save(order);

        saveProtobufOutboxEvent(saved, OrderEventType.ORDER_CREATED, flow);

        return new OrderDto(saved.getId(), saved.getCustomerName(), saved.getAmount(), saved.getStatus());
    }

    public Optional<OrderDto> getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(order -> new OrderDto(order.getId(), order.getCustomerName(), order.getAmount(),
                        order.getStatus()));
    }

    public Optional<PaymentDto> getPaymentByOrderId(Long id) {
        return paymentServiceClient.getPaymentByOrderId(id);
    }

    private void saveOutboxEvent(Order order, OrderEventType eventType) {
        OrderEvent orderEvent = new OrderEvent(
                eventType,
                order.getId(),
                order.getCustomerName(),
                order.getAmount(),
                order.getStatus()
        );

        OutboxEvent event = new OutboxEvent();
        event.setAggregateType(AggregateType.ORDER);
        event.setAggregateId(order.getId());
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(eventType);
        event.setPayload(objectMapper.writeValueAsString(orderEvent));
        event.setCreatedAt(LocalDateTime.now());

        outboxEventRepository.save(event);
    }

    private void saveProtobufOutboxEvent(Order order, OrderEventType eventType, ProcessingFlow flow) {
        final String eventId = UUID.randomUUID().toString();

        com.demo.protobuf.order.event.OrderEvent protobufEvent = com.demo.protobuf.order.event.OrderEvent.newBuilder()
                .setEventId(eventId)
                .setEventType(eventType.name())
                .setOrderId(order.getId())
                .setCustomerName(order.getCustomerName())
                .setAmount(order.getAmount().toString())
                .setStatus(com.demo.protobuf.order.enums.OrderStatus.ORDER_STATUS_CREATED)
                .build();

        ProtobufOutboxEvent event = new ProtobufOutboxEvent();
        event.setAggregateType(AggregateType.ORDER);
        event.setAggregateId(order.getId());
        event.setEventId(eventId);
        event.setEventType(eventType);
        event.setPayload(protobufEvent.toByteArray());
        event.setTargetTopic(resolveTargetTopic(flow).getTopicName());
        event.setCreatedAt(LocalDateTime.now());

        protobufOutboxEventRepository.save(event);
    }

    private OutboxTargetTopic resolveTargetTopic(ProcessingFlow flow) {
        return switch (flow) {
            case CLASSIC -> OutboxTargetTopic.ORDER_EVENTS_PROTOBUF;
            case FUNCTIONAL -> OutboxTargetTopic.ORDER_EVENTS_FUNCTIONS;
        };
    }
}
