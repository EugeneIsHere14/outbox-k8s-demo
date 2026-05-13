package com.demo.order.entity;

import com.demo.order.enums.AggregateType;
import com.demo.order.enums.OrderEventType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "protobuf_outbox_events")
@Data
public class ProtobufOutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AggregateType aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private Long aggregateId;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "event_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderEventType eventType;

    @Lob
    @Column(name = "payload", nullable = false, columnDefinition = "BLOB")
    private byte[] payload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}