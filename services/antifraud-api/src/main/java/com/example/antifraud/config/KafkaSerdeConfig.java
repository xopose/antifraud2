package com.example.antifraud.config;

import com.example.antifraud.dto.TransactionIngestEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaConsumerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

@Configuration
public class KafkaSerdeConfig {

  @Bean
  public DefaultKafkaProducerFactoryCustomizer producerCustomizer() {
    return (factory) -> factory.updateConfigs(Map.of(
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class
    ));
  }

  @Bean
  public DefaultKafkaConsumerFactoryCustomizer consumerCustomizer() {
    return (factory) -> factory.updateConfigs(Map.of(
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class,
        JsonDeserializer.VALUE_DEFAULT_TYPE, TransactionIngestEvent.class.getName(),
        JsonDeserializer.TRUSTED_PACKAGES, "*"
    ));
  }
}
