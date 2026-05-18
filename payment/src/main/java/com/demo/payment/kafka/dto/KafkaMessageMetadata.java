package com.demo.payment.kafka.dto;

public record KafkaMessageMetadata(String topic, Integer partition, Long offset, Object key) {
}
