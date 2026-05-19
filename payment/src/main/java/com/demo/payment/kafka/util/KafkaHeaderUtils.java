package com.demo.payment.kafka.util;

import com.demo.payment.kafka.dto.KafkaMessageMetadata;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;

public final class KafkaHeaderUtils {

    private KafkaHeaderUtils() {
        // Private constructor to prevent instantiation
    }

    public static KafkaMessageMetadata extractMetadata(final MessageHeaders headers) {
        return new KafkaMessageMetadata(
                headers.get(KafkaHeaders.RECEIVED_TOPIC, String.class),
                headers.get(KafkaHeaders.RECEIVED_PARTITION, Integer.class),
                headers.get(KafkaHeaders.OFFSET, Long.class),
                headers.get(KafkaHeaders.RECEIVED_KEY)
        );
    }
}
