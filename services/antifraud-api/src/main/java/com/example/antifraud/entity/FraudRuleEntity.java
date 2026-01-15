package com.example.antifraud.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "fraud_rules")
public class FraudRuleEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name="code", nullable=false, unique=true, length=64)
  private String code;

  @Column(name="title", nullable=false, length=128)
  private String title;

  @Column(name="description", nullable=false)
  private String description;

  @Column(name="threshold")
  private Double threshold;

  @Column(name="enabled", nullable=false)
  private short enabled;

  @Column(name="severity", nullable=false, length=16)
  private String severity;

  @Column(name="created_at", nullable=false)
  private Instant createdAt;

  public Integer getId() { return id; }
  public String getCode() { return code; }
  public String getTitle() { return title; }
  public String getDescription() { return description; }
  public Double getThreshold() { return threshold; }
  public boolean isEnabled() { return enabled != 0; }
  public String getSeverity() { return severity; }
  public Instant getCreatedAt() { return createdAt; }

  public void setThreshold(Double threshold) { this.threshold = threshold; }
  public void setEnabled(boolean enabled) { this.enabled = (short)(enabled ? 1 : 0); }
  public void setSeverity(String severity) { this.severity = severity; }
}
