package com.example.antifraud.kafka;

import com.example.antifraud.dto.TransactionIngestEvent;
import com.example.antifraud.service.TransactionService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class TransactionConsumer {

  private final TransactionService service;
  private final Counter ingested;

  public TransactionConsumer(TransactionService service, MeterRegistry registry) {
    this.service = service;
    this.ingested = registry.counter("antifraud_kafka_transactions_ingested_total");
  }

  @KafkaListener(topics = "${antifraud.kafka.topic:transactions}", groupId = "antifraud-api")
  public void onMessage(TransactionIngestEvent event
  ) {
    service.ingest(event, event);
    ingested.increment();
  }
}
