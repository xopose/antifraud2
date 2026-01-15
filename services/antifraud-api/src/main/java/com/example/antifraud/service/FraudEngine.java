package com.example.antifraud.service;

import com.example.antifraud.dto.TransactionIngestEvent;
import com.example.antifraud.entity.FraudAlertEntity;
import com.example.antifraud.entity.FraudRuleEntity;
import com.example.antifraud.entity.TransactionEntity;
import com.example.antifraud.repo.FraudRuleRepository;
import com.example.antifraud.repo.TransactionRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class FraudEngine {

  // Rule codes as constants to avoid typos
  private static final String RULE_HIGH_AMOUNT = "HIGH_AMOUNT";
  private static final String RULE_VELOCITY   = "VELOCITY";
  private static final String RULE_GEO_JUMP   = "GEO_JUMP";

  private final FraudRuleRepository ruleRepo;
  private final TransactionRepository txRepo;
  private final Counter alertsCreated;

  public FraudEngine(FraudRuleRepository ruleRepo, TransactionRepository txRepo, MeterRegistry registry) {
    this.ruleRepo = ruleRepo;
    this.txRepo = txRepo;
    this.alertsCreated = registry.counter("antifraud_fraud_alerts_created_total");
  }

  public EvaluationResult evaluate(TransactionIngestEvent tx) {
    TransactionEntity curr = txRepo.findByTransactionId(tx.transaction_id())
            .orElseThrow(() -> new IllegalStateException(
                    "Transaction not found in DB (evaluate must be called after save): " + tx.transaction_id()
            ));

    final Instant currTime = curr.getCreatedAt();


    Map<String, FraudRuleEntity> rules = loadRules();

    List<FraudAlertEntity> alerts = new ArrayList<>();

    FraudRuleEntity r1 = rules.get(RULE_HIGH_AMOUNT);
    if (isEnabled(r1)) {
      double threshold = Optional.ofNullable(r1.getThreshold()).orElse(10000.0);
      if (tx.amount().compareTo(BigDecimal.valueOf(threshold)) > 0) {
        alerts.add(FraudAlertEntity.of(
                curr.getTransactionId(),
                curr.getAccountId(),
                r1.getCode(),
                r1.getSeverity(),
                "Amount " + tx.amount() + " > " + threshold,
                Instant.now()
        ));
      }
    }

    FraudRuleEntity r2 = rules.get(RULE_VELOCITY);
    if (isEnabled(r2) && alerts.isEmpty()) {
      long limit = Math.round(Optional.ofNullable(r2.getThreshold()).orElse(5.0));
      if (limit < 1) limit = 1;

      Instant from = currTime.minus(Duration.ofMinutes(2));

      long cnt = txRepo.countByAccountSince(curr.getAccountId(), from);

      if (cnt >= limit) {
        alerts.add(FraudAlertEntity.of(
                curr.getTransactionId(),
                curr.getAccountId(),
                r2.getCode(),
                r2.getSeverity(),
                "More than " + limit + " transactions in 2 minutes",
                Instant.now()
        ));
      }
    }

    FraudRuleEntity r3 = rules.get(RULE_GEO_JUMP);
    if (isEnabled(r3) && alerts.isEmpty()) {
      long thresholdSeconds = Math.round(Optional.ofNullable(r3.getThreshold()).orElse(60.0));
      if (thresholdSeconds < 0) thresholdSeconds = 0;

      long finalThresholdSeconds = thresholdSeconds;
      txRepo.findTopByAccountIdAndTransactionIdNotOrderByCreatedAtDesc(
              curr.getAccountId(),
              curr.getTransactionId()
      ).ifPresent(prev -> {
        String prevCountry = normalizeCountry(prev.getCountry());
        String currCountry = normalizeCountry(curr.getCountry());

        if (prevCountry.isEmpty() || currCountry.isEmpty()) return;
        if (prevCountry.equals(currCountry)) return;

        Instant prevTime = prev.getCreatedAt();

        Duration diff = Duration.between(prevTime, currTime);

        if (diff.isNegative()) return;

        if (diff.compareTo(Duration.ofSeconds(finalThresholdSeconds)) < 0) {
          alerts.add(FraudAlertEntity.of(
                  curr.getTransactionId(),
                  curr.getAccountId(),
                  r3.getCode(),
                  r3.getSeverity(),
                  "Country changed from " + prevCountry + " to " + currCountry +
                          " within " + diff.toSeconds() + "s",
                  Instant.now()
          ));
        }
      });
    }

    if (!alerts.isEmpty()) {
      alertsCreated.increment(alerts.size());
    }

    return new EvaluationResult(alerts);
  }

  private boolean isEnabled(FraudRuleEntity r) {
    return r != null && r.isEnabled();
  }

  private String normalizeCountry(String country) {
    return country == null ? "" : country.trim().toUpperCase(Locale.ROOT);
  }

  private Map<String, FraudRuleEntity> loadRules() {
    Map<String, FraudRuleEntity> map = new HashMap<>();
    for (FraudRuleEntity r : ruleRepo.findAll()) {
      map.put(r.getCode(), r);
    }
    return map;
  }

  public record EvaluationResult(List<FraudAlertEntity> alerts) {}
}
