package com.example.antifraud.entity;

import com.example.antifraud.dto.TransactionIngestEvent;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions")
public class TransactionEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name="transaction_id", nullable=false, unique=true, length=64)
  private String transactionId;

  @Column(name="created_at", nullable=false, updatable = false)
  private Instant createdAt;

  @Column(name="account_id", nullable=false)
  private Long accountId;

  @Column(name="amount", nullable=false, precision = 18, scale = 2)
  private BigDecimal amount;

  @Column(name="currency", nullable=false, length=8)
  private String currency;

  @Column(name="merchant", nullable=false, length=128)
  private String merchant;

  @Column(name="country", nullable=false, length=8)
  private String country;

  @Column(name="status", nullable=false, length=16)
  private String status;

  @Column(name="ingested_at", nullable=false)
  private Instant ingestedAt;

  @Column(name="source", nullable=false, length=16)
  private String source;

  @Type(JsonBinaryType.class)
  @Column(columnDefinition = "jsonb", nullable = false)
  private TransactionIngestEvent payload;

  @PrePersist
  void onCreate() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
    if (status == null) {
      status = "APPROVED";
    }
  }

  public static TransactionEntity of(
          String txId,
          Long accountId,
          BigDecimal amount,
          String currency,
          String merchant,
          String country,
          Instant ingestedAt,
          String source,
          TransactionIngestEvent payloadJson
  ) {
    var e = new TransactionEntity();
    e.transactionId = txId;
    e.accountId = accountId;
    e.amount = amount;
    e.currency = currency;
    e.merchant = merchant;
    e.country = country;
    e.ingestedAt = ingestedAt;
    e.source = source;
    e.payload = payloadJson;
    return e;
  }

  public Long getId() { return id; }
  public String getTransactionId() { return transactionId; }
  public Instant getCreatedAt() { return createdAt; }
  public Long getAccountId() { return accountId; }
  public BigDecimal getAmount() { return amount; }
  public String getCurrency() { return currency; }
  public String getMerchant() { return merchant; }
  public String getCountry() { return country; }
  public String getStatus() { return status; }
  public Instant getIngestedAt() { return ingestedAt; }
  public String getSource() { return source; }
  public TransactionIngestEvent getPayload() { return payload; }
}
