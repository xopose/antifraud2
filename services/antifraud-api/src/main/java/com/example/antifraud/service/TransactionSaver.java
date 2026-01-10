package com.example.antifraud.service;

import com.example.antifraud.dto.TransactionIngestEvent;
import com.example.antifraud.entity.TransactionEntity;
import com.example.antifraud.repo.TransactionRepository;
import io.micrometer.core.instrument.Counter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class TransactionSaver {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean saveTransaction(TransactionRepository txRepo, Counter txSaved, TransactionIngestEvent tx) {
        txRepo.save(TransactionEntity.of(
                tx.transaction_id(), tx.account_id(), tx.amount(),
                tx.currency(), tx.merchant(), tx.country(),
                Instant.now(), tx.source(), tx
        ));
        txSaved.increment();
        return true;
    }
}
