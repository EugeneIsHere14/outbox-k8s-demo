package com.demo.order.entity;

import com.demo.order.enums.AggregateType;
import com.demo.order.enums.OrderEventType;
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
    private OrderEventType eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private LocalDateTime createdAt;
}
