package com.demo.payment.kafka.deserializer;

import com.demo.protobuf.order.event.OrderEvent;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

public class OrderEventProtobufDeserializer implements Deserializer<OrderEvent> {

    @Override
    public OrderEvent deserialize(final String topic, final byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }

        try {
            return OrderEvent.parseFrom(data);
        } catch (InvalidProtocolBufferException ex) {
            throw new SerializationException("Failed to deserialize protobuf OrderEvent", ex);
        }
    }
}
