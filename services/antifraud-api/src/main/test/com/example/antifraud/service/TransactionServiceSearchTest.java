package com.example.antifraud.service;

import com.example.antifraud.entity.TransactionEntity;
import com.example.antifraud.repo.FraudAlertRepository;
import com.example.antifraud.repo.TransactionRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceSearchTest {

    private TransactionService createService(TransactionRepository txRepo) {
        FraudAlertRepository alertRepo = mock(FraudAlertRepository.class);
        FraudEngine engine = mock(FraudEngine.class);
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        TransactionSaver saver = mock(TransactionSaver.class);
        TransactionStatusUpdater statusUpdater = mock(TransactionStatusUpdater.class);

        return new TransactionService(txRepo, alertRepo, engine, registry, saver, statusUpdater);
    }

    @Test
    void searchByTransactionIdHasPriority() {
        TransactionRepository txRepo = mock(TransactionRepository.class);
        TransactionEntity tx = new TransactionEntity();
        when(txRepo.findByTransactionId("TX1")).thenReturn(Optional.of(tx));

        TransactionService service = createService(txRepo);

        List<TransactionEntity> result = service.search("TX1", 42L, null, null, null);

        assertEquals(1, result.size());
        assertSame(tx, result.get(0));

        verify(txRepo).findByTransactionId("TX1");
        verify(txRepo, never()).findTop200ByAccountIdOrderByCreatedAtDesc(anyLong());
        verify(txRepo, never()).findAll();
    }

    @Test
    void searchByAccountIdWhenNoTransactionId() {
        TransactionRepository txRepo = mock(TransactionRepository.class);
        TransactionEntity tx1 = new TransactionEntity();
        TransactionEntity tx2 = new TransactionEntity();

        when(txRepo.findTop200ByAccountIdOrderByCreatedAtDesc(7L))
                .thenReturn(List.of(tx1, tx2));

        TransactionService service = createService(txRepo);

        List<TransactionEntity> result = service.search(null, 7L, null, null, null);

        assertEquals(2, result.size());
        assertTrue(result.contains(tx1));
        assertTrue(result.contains(tx2));

        verify(txRepo).findTop200ByAccountIdOrderByCreatedAtDesc(7L);
        verify(txRepo, never()).findByTransactionId(anyString());
        verify(txRepo, never()).findAll();
    }

    @Test
    void searchFallsBackToAllWhenNoFilters() {
        TransactionRepository txRepo = mock(TransactionRepository.class);
        TransactionEntity tx1 = new TransactionEntity();
        TransactionEntity tx2 = new TransactionEntity();

        when(txRepo.findAll()).thenReturn(List.of(tx1, tx2));

        TransactionService service = createService(txRepo);

        List<TransactionEntity> result = service.search(null, null, null, Instant.now().minusSeconds(3600), Instant.now());

        assertEquals(2, result.size());
        verify(txRepo).findAll();
        verify(txRepo, never()).findByTransactionId(anyString());
        verify(txRepo, never()).findTop200ByAccountIdOrderByCreatedAtDesc(anyLong());
    }
}
