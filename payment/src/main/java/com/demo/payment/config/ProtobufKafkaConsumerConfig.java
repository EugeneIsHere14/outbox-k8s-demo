package com.demo.payment.config;

import com.demo.payment.kafka.deserializer.OrderEventProtobufDeserializer;
import com.demo.protobuf.order.event.OrderEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.Map;

@Configuration
@ConditionalOnBooleanProperty(name = "app.kafka.enabled")
public class ProtobufKafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, OrderEvent> protobufOrderEventConsumerFactory(final KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, OrderEventProtobufDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "protobuf-payment-group");

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderEvent> protobufKafkaListenerContainerFactory(
            final ConsumerFactory<String, OrderEvent> protobufOrderEventConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, OrderEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(protobufOrderEventConsumerFactory);

        return factory;
    }
}
