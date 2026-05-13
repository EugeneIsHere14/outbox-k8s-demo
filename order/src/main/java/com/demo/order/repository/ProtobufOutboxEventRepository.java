package com.demo.order.repository;

import com.demo.order.entity.ProtobufOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProtobufOutboxEventRepository extends JpaRepository<ProtobufOutboxEvent, Long> {
}
