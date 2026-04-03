package com.demo.payment.entity;

import com.demo.payment.enums.AggregateType;
import com.demo.payment.enums.PaymentEventType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events")
@Data
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AggregateType aggregateType;

    private Long aggregateId;

    private String eventId;

    @Enumerated(EnumType.STRING)
    private PaymentEventType eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
