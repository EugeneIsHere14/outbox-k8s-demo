package com.demo.payment.service;

import com.demo.payment.dto.PaymentDto;
import com.demo.payment.dto.event.PaymentProcessingResult;
import com.demo.payment.entity.OutboxEvent;
import com.demo.payment.entity.Payment;
import com.demo.payment.entity.ProcessedEvent;
import com.demo.payment.enums.AggregateType;
import com.demo.payment.enums.PaymentEventType;
import com.demo.payment.enums.PaymentStatus;
import com.demo.payment.repository.OutboxEventRepository;
import com.demo.payment.repository.PaymentRepository;
import com.demo.payment.repository.ProcessedEventRepository;
import com.demo.protobuf.order.event.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ProcessedEventRepository processedEventRepository;

    public PaymentService(final PaymentRepository paymentRepository,
                          final OutboxEventRepository outboxEventRepository,
                          final ProcessedEventRepository processedEventRepository) {
        this.paymentRepository = paymentRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    public PaymentDto createPayment(PaymentDto paymentDto) {
        Payment payment = new Payment();
        payment.setOrderId(paymentDto.orderId());
        payment.setAmount(paymentDto.amount());
        payment.setStatus(PaymentStatus.PENDING);
        Payment saved = paymentRepository.save(payment);

        saveOutboxEvent(saved, PaymentEventType.PAYMENT_CREATED);

        return new PaymentDto(saved.getId(), saved.getOrderId(), saved.getAmount(), saved.getStatus());
    }

    public Optional<PaymentDto> getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(payment -> new PaymentDto(payment.getId(), payment.getOrderId(), payment.getAmount(), payment.getStatus()));
    }

    private void saveOutboxEvent(Payment payment, PaymentEventType eventType) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType(AggregateType.PAYMENT);
        event.setAggregateId(payment.getId());
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(eventType);
        event.setPayload(String.format("{\"paymentId\":%d,\"orderId\":%d,\"amount\":%s,\"status\":\"%s\"}",
                payment.getId(), payment.getOrderId(), payment.getAmount(), payment.getStatus()));
        event.setCreatedAt(LocalDateTime.now());

        outboxEventRepository.save(event);
    }

    @Transactional
    public PaymentProcessingResult reservePayment(OrderEvent orderEvent) {
        if (processedEventRepository.existsById(orderEvent.getEventId())) {
            log.info("Order event already processed. eventId={}, orderId={}, eventType={}",
                    orderEvent.getEventId(),
                    orderEvent.getOrderId(),
                    orderEvent.getEventType()
            );

            return PaymentProcessingResult.alreadyProcessed(
                    orderEvent.getEventId(),
                    orderEvent.getOrderId(),
                    orderEvent.getCustomerName(),
                    new BigDecimal(orderEvent.getAmount())
            );
        }

        PaymentProcessingResult result = PaymentProcessingResult.reserved(
                orderEvent.getEventId(),
                orderEvent.getOrderId(),
                orderEvent.getCustomerName(),
                new BigDecimal(orderEvent.getAmount())
        );

        ProcessedEvent processedEvent = new ProcessedEvent();
        processedEvent.setEventId(orderEvent.getEventId());
        processedEvent.setAggregateId(orderEvent.getOrderId());
        processedEvent.setEventType(orderEvent.getEventType());

        processedEventRepository.save(processedEvent);

        log.info("Payment reserved. eventId={}, orderId={}, eventType={}",
                orderEvent.getEventId(),
                orderEvent.getOrderId(),
                orderEvent.getEventType()
        );

        return result;
    }
}
