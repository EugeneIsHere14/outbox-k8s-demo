package com.demo.payment.protobuf.codec;

import com.demo.protobuf.order.event.OrderEvent;
import com.demo.protobuf.payment.event.PaymentEvent;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.kafka.common.errors.SerializationException;

public final class ProtobufCodecUtils {

    private ProtobufCodecUtils() {
        // Private constructor to prevent instantiation
    }

    public static OrderEvent deserializeToOrderEvent(byte[] data) {
        if (data == null || data.length == 0) {
            throw new SerializationException("OrderEvent payload cannot be null or empty");
        }

        try {
            return OrderEvent.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new SerializationException("Failed to deserialize protobuf OrderEvent", e);
        }
    }

    public static byte[] serializePaymentEvent(final PaymentEvent paymentEvent) {
        if (paymentEvent == null) {
            throw new IllegalArgumentException("PaymentEvent cannot be null");
        }

        return paymentEvent.toByteArray();
    }
}
