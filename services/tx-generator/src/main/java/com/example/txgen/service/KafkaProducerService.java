package com.example.txgen.service;

import com.example.txgen.dto.TransactionIngestEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class KafkaProducerService implements DisposableBean {

    private final Producer<String, byte[]> producer;
    private final ObjectMapper mapper;
    private final String topic;

    public KafkaProducerService() {
        String bootstrap = getenvFirstNonBlank("SPRING_KAFKA_BOOTSTRAP_SERVERS", "KAFKA_BOOTSTRAP");
        this.topic = getenvOrDefault("TOPIC", "transactions");

        if (bootstrap == null || bootstrap.isBlank()) {
            throw new IllegalStateException("Kafka bootstrap is not set. Set SPRING_KAFKA_BOOTSTRAP_SERVERS or KAFKA_BOOTSTRAP");
        }

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        props.put(ProducerConfig.RETRIES_CONFIG, "10");
        props.put(ProducerConfig.LINGER_MS_CONFIG, "10");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());

        this.producer = new KafkaProducer<>(props);

        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void send(TransactionIngestEvent event) {
        try {
            byte[] payload = mapper.writeValueAsBytes(event);
            ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, event.transaction_id(), payload);
            producer.send(record);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send Kafka message", e);
        }
    }

    @Override
    public void destroy() {
        producer.close();
    }

    private static String getenvOrDefault(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }

    private static String getenvFirstNonBlank(String... keys) {
        for (String k : keys) {
            String v = System.getenv(k);
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }
}
