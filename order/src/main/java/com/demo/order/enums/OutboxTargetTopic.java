package com.demo.order.enums;

public enum OutboxTargetTopic {
    ORDER_EVENTS_PROTOBUF("order-events-protobuf"),
    ORDER_EVENTS_FUNCTIONS("order-events-functions");

    private final String topicName;

    OutboxTargetTopic(String topicName) {
        this.topicName = topicName;
    }

    public String getTopicName() {
        return topicName;
    }
}
