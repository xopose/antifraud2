package com.example.antifraud.service;

import com.example.antifraud.dto.TransactionIngestEvent;
import com.example.antifraud.dto.TransactionResponse;
import com.example.antifraud.entity.FraudAlertEntity;
import com.example.antifraud.entity.TransactionEntity;
import com.example.antifraud.repo.FraudAlertRepository;
import com.example.antifraud.repo.TransactionRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class TransactionService {

  private final TransactionRepository txRepo;
  private final FraudAlertRepository alertRepo;
  private final FraudEngine engine;
  private final Counter txSaved;

  private final TransactionSaver transactionSaver;
  private final TransactionStatusUpdater statusUpdater;

  public TransactionService(TransactionRepository txRepo, FraudAlertRepository alertRepo, FraudEngine engine, MeterRegistry registry, TransactionSaver transactionSaver, TransactionStatusUpdater statusUpdater) {
    this.txRepo = txRepo;
    this.alertRepo = alertRepo;
    this.engine = engine;
    this.txSaved = registry.counter("antifraud_transactions_saved_total");
    this.transactionSaver = transactionSaver;
      this.statusUpdater = statusUpdater;
  }

  public TransactionResponse ingest(TransactionIngestEvent tx, TransactionIngestEvent payloadJson) {

    try {
      transactionSaver.saveTransaction(txRepo, txSaved, tx);
    } catch (DataIntegrityViolationException e) {
      return new TransactionResponse("DUPLICATE", List.of());
    }

    var eval = engine.evaluate(tx);

    if (!eval.alerts().isEmpty()) {

      alertRepo.saveAll(eval.alerts());

      statusUpdater.decline(tx.transaction_id());

      return new TransactionResponse(
              "FRAUD",
              eval.alerts().stream().map(FraudAlertEntity::getRuleCode).toList()
      );
    }

    return new TransactionResponse("OK", List.of());
  }



  public List<TransactionEntity> search(String transactionId, Long accountId, String status, Instant from, Instant to) {

    if (transactionId != null && !transactionId.isBlank()) {
      return txRepo.findByTransactionId(transactionId).map(List::of).orElse(List.of());
    }
    if (accountId != null) {
      return txRepo.findTop200ByAccountIdOrderByCreatedAtDesc(accountId);
    }
    return txRepo.findAll().stream().limit(200).toList();
  }
}
