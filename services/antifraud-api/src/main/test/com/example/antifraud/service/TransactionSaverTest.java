package com.example.antifraud.service;

import com.example.antifraud.dto.TransactionIngestEvent;
import com.example.antifraud.entity.TransactionEntity;
import com.example.antifraud.repo.TransactionRepository;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionSaverTest {

    @Test
    void saveTransactionPersistsEntityAndIncrementsCounter() {
        TransactionRepository txRepo = mock(TransactionRepository.class);
        Counter counter = mock(Counter.class);

        when(txRepo.save(any(TransactionEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionIngestEvent event = new TransactionIngestEvent(
                "tx-1",
                1L,
                new BigDecimal("50.00"),
                "RUB",
                "RU",
                "SHOP",
                "kafka"
        );

        TransactionSaver saver = new TransactionSaver();

        boolean result = saver.saveTransaction(txRepo, counter, event);

        assertTrue(result);
        verify(txRepo).save(any(TransactionEntity.class));
        verify(counter).increment();
    }
}
