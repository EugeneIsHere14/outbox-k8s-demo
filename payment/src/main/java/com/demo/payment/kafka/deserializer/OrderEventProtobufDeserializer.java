package com.demo.payment.kafka.deserializer;

import com.demo.payment.protobuf.codec.ProtobufCodecUtils;
import com.demo.protobuf.order.event.OrderEvent;
import org.apache.kafka.common.serialization.Deserializer;

public class OrderEventProtobufDeserializer implements Deserializer<OrderEvent> {

    @Override
    public OrderEvent deserialize(final String topic, final byte[] data) {
        return ProtobufCodecUtils.deserializeToOrderEvent(data);
    }
}
