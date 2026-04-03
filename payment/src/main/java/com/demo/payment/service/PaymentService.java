package com.demo.payment.service;

import com.demo.payment.dto.PaymentDto;
import com.demo.payment.entity.OutboxEvent;
import com.demo.payment.entity.Payment;
import com.demo.payment.enums.AggregateType;
import com.demo.payment.enums.PaymentEventType;
import com.demo.payment.enums.PaymentStatus;
import com.demo.payment.repository.OutboxEventRepository;
import com.demo.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OutboxEventRepository outboxEventRepository;

    public PaymentService(final PaymentRepository paymentRepository, final OutboxEventRepository outboxEventRepository) {
        this.paymentRepository = paymentRepository;
        this.outboxEventRepository = outboxEventRepository;
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
}
