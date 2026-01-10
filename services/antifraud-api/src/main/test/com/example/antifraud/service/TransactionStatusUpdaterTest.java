package com.example.antifraud.service;

import com.example.antifraud.repo.TransactionRepository;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class TransactionStatusUpdaterTest {

    @Test
    void declineUpdatesStatusToDeclined() {
        TransactionRepository txRepo = mock(TransactionRepository.class);
        TransactionStatusUpdater updater = new TransactionStatusUpdater(txRepo);

        updater.decline("tx-123");

        verify(txRepo).updateStatusByTransactionId("tx-123", "DECLINED");
    }
}
