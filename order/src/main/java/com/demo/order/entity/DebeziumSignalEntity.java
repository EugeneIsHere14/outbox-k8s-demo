package com.demo.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "debezium_signals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebeziumSignalEntity {

    @Id
    private String id;

    private String type;

    @Column(length = 2048)
    private String data;
}
