package com.example.antifraud.kafka;

import com.example.antifraud.dto.TransactionIngestEvent;
import com.example.antifraud.service.TransactionService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TransactionConsumerTest {

    @Test
    void onMessageCallsServiceAndIncrementsMetric() {
        TransactionService service = mock(TransactionService.class);
        SimpleMeterRegistry registry = new SimpleMeterRegistry();

        TransactionConsumer consumer = new TransactionConsumer(service, registry);

        TransactionIngestEvent event = new TransactionIngestEvent(
                "tx-1",
                1L,
                new BigDecimal("100.00"),
                "EUR",
                "DE",
                "SHOP",
                "kafka"
        );

        consumer.onMessage(event);

        verify(service).ingest(event, event);

        Counter counter = registry.counter("antifraud_kafka_transactions_ingested_total");
        assertEquals(1.0, counter.count(), 1e-6);
    }
}
