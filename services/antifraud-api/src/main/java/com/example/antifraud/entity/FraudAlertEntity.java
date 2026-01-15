package com.example.antifraud.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "fraud_alerts")
public class FraudAlertEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name="transaction_id", nullable=false, length=64)
  private String transactionId;

  @Column(name="account_id", nullable=false)
  private Long accountId;

  @Column(name="rule_code", nullable=false, length=64)
  private String ruleCode;

  @Column(name="severity", nullable=false, length=16)
  private String severity;

  @Column(name="description", nullable=false)
  private String description;

  @Column(name="created_at", nullable=false)
  private Instant createdAt;

  @Column(name="resolved", nullable=false)
  private short resolved = 0;

  public FraudAlertEntity() {}

  public static FraudAlertEntity of(String txId, Long accountId, String ruleCode, String severity, String description, Instant createdAt) {
    var a = new FraudAlertEntity();
    a.transactionId = txId;
    a.accountId = accountId;
    a.ruleCode = ruleCode;
    a.severity = severity;
    a.description = description;
    a.createdAt = createdAt;
    a.resolved = 0;
    return a;
  }

  public Long getId() { return id; }
  public String getTransactionId() { return transactionId; }
  public Long getAccountId() { return accountId; }
  public String getRuleCode() { return ruleCode; }
  public String getSeverity() { return severity; }
  public String getDescription() { return description; }
  public Instant getCreatedAt() { return createdAt; }
  public short getResolved() { return resolved; }
}
