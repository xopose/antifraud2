package com.example.antifraud.service;

import com.example.antifraud.repo.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionStatusUpdater {

    private final TransactionRepository txRepo;

    public TransactionStatusUpdater(TransactionRepository txRepo) {
        this.txRepo = txRepo;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void decline(String transactionId) {
        txRepo.updateStatusByTransactionId(transactionId, "DECLINED");
    }
}