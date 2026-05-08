package com.demo.order.repository;

import com.demo.order.entity.DebeziumSignalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DebeziumSignalRepository extends JpaRepository<DebeziumSignalEntity, String> {
}
