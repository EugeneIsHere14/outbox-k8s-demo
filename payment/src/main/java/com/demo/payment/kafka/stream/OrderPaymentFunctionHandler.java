package com.demo.payment.kafka.stream;

import com.demo.payment.dto.event.PaymentProcessingResult;
import com.demo.payment.kafka.dto.KafkaMessageMetadata;
import com.demo.payment.kafka.util.KafkaHeaderUtils;
import com.demo.payment.protobuf.codec.ProtobufCodecUtils;
import com.demo.payment.protobuf.mapper.PaymentEventMapper;
import com.demo.payment.service.PaymentService;
import com.demo.protobuf.order.event.OrderEvent;
import com.demo.protobuf.payment.event.PaymentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Function;

@Configuration
@Slf4j
public class OrderPaymentFunctionHandler {

    @Bean
    public Function<Message<byte[]>, Message<OrderEvent>> orderEventDeserializer() {
        return message -> {
            if (message == null) {
                log.error("Retrieved message is null");
                throw new IllegalStateException("Message cannot be null");
            }

            KafkaMessageMetadata metadata = KafkaHeaderUtils.extractMetadata(message.getHeaders());
            log.info("Deserializing OrderEvent. topic={}, partition={}, offset={}, key={}. ", metadata.topic(),
                    metadata.partition(), metadata.offset(), metadata.key());

            return MessageBuilder
                    .withPayload(ProtobufCodecUtils.deserializeToOrderEvent(message.getPayload()))
                    .copyHeaders(message.getHeaders())
                    .build();
        };
    }

    @Bean
    public Function<Message<OrderEvent>, Message<PaymentProcessingResult>> paymentReservationProcessor(final PaymentService paymentService) {
        return message -> {
            if (message == null) {
                log.error("Retrieved message is null");
                throw new IllegalStateException("Message cannot be null");
            }

            KafkaMessageMetadata metadata = KafkaHeaderUtils.extractMetadata(message.getHeaders());
            log.info("Processing payment reservation. topic={}, partition={}, offset={}, key={}. ", metadata.topic(),
                    metadata.partition(), metadata.offset(), metadata.key());

            return MessageBuilder
                    .withPayload(paymentService.reservePayment(message.getPayload()))
                    .copyHeaders(message.getHeaders())
                    .build();
        };
    }

    @Bean
    public Function<Message<PaymentProcessingResult>, Message<byte[]>> paymentEventSerializer(final PaymentEventMapper paymentEventMapper) {
        return message -> {
            if (message == null) {
                log.error("Retrieved message is null");
                throw new IllegalStateException("Message cannot be null");
            }

            KafkaMessageMetadata metadata = KafkaHeaderUtils.extractMetadata(message.getHeaders());
            log.info("Serializing PaymentEvent. topic={}, partition={}, offset={}, key={}. ", metadata.topic(),
                    metadata.partition(), metadata.offset(), metadata.key());

            PaymentEvent paymentEvent = paymentEventMapper.toProtobuf(message.getPayload());

            return MessageBuilder
                    .withPayload(ProtobufCodecUtils.serializePaymentEvent(paymentEvent))
                    .copyHeaders(message.getHeaders())
                    .build();
        };
    }
}
