package com.example.txgen;

import com.example.txgen.dto.TransactionIngestEvent;
import com.example.txgen.service.KafkaProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class GeneratorRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(GeneratorRunner.class);

    private final KafkaProducerService producer;
    private final int ratePerSec;

    public GeneratorRunner(KafkaProducerService producer) {
        this.producer = producer;
        this.ratePerSec = parsePositiveInt(System.getenv("RATE_PER_SEC"), 20);
    }

    @Override
    public void run(String... args) throws Exception {
        long delayMs = Math.max(1L, 1000L / Math.max(1, ratePerSec));
        log.info("tx-generator started. RATE_PER_SEC={}, delayMs={}", ratePerSec, delayMs);

        while (true) {
            producer.send(TransactionIngestEvent.random());
            Thread.sleep(delayMs);
        }
    }

    private static int parsePositiveInt(String v, int def) {
        try {
            if (v == null || v.isBlank()) return def;
            int x = Integer.parseInt(v.trim());
            return x > 0 ? x : def;
        } catch (Exception e) {
            return def;
        }
    }
}
