package com.demo.payment.kafka.stream;

import com.demo.payment.dto.event.PaymentProcessingResult;
import com.demo.payment.protobuf.codec.ProtobufCodecUtils;
import com.demo.payment.protobuf.mapper.PaymentEventMapper;
import com.demo.payment.service.PaymentService;
import com.demo.protobuf.order.event.OrderEvent;
import com.demo.protobuf.payment.event.PaymentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
@Slf4j
public class OrderPaymentFunctionHandler {

    @Bean
    public Function<byte[], OrderEvent> orderEventDeserializer() {
        return payload -> {
            if (payload == null || payload.length == 0) {
                log.error("Retrieved empty OrderEvent payload");
                throw new IllegalStateException("OrderEvent payload cannot be empty");
            }

            log.info("Deserializing OrderEvent. payloadSize={}", payload.length);

            return ProtobufCodecUtils.deserializeToOrderEvent(payload);
        };
    }

    @Bean
    public Function<OrderEvent, PaymentProcessingResult> paymentReservationProcessor(final PaymentService paymentService) {
        return orderEvent -> {
            if (orderEvent == null) {
                log.error("Retrieved OrderEvent is null");
                throw new IllegalStateException("OrderEvent cannot be null");
            }

            log.info("Processing payment reservation. orderId={}", orderEvent.getOrderId());

            return paymentService.reservePayment(orderEvent);
        };
    }

    @Bean
    public Function<PaymentProcessingResult, byte[]> paymentEventSerializer(final PaymentEventMapper paymentEventMapper) {
        return paymentProcessingResult -> {
            if (paymentProcessingResult == null) {
                log.error("Retrieved PaymentProcessingResult is null");
                throw new IllegalStateException("PaymentProcessingResult cannot be null");
            }

            log.info("Serializing PaymentEvent. orderId={}", paymentProcessingResult.orderId());

            PaymentEvent paymentEvent = paymentEventMapper.toProtobuf(paymentProcessingResult);

            return ProtobufCodecUtils.serializePaymentEvent(paymentEvent);
        };
    }
}
