package com.example.antifraud.service;

import com.example.antifraud.entity.FraudRuleEntity;
import com.example.antifraud.repo.FraudRuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RuleService {
  private final FraudRuleRepository repo;

  public RuleService(FraudRuleRepository repo) {
    this.repo = repo;
  }

  public List<FraudRuleEntity> all() {
    return repo.findAll();
  }

  public FraudRuleEntity update(String code, Double threshold, Boolean enabled, String severity) {
    var rule = repo.findByCode(code).orElseThrow(() -> new IllegalArgumentException("Rule not found: " + code));
    if (threshold != null) rule.setThreshold(threshold);
    if (enabled != null) rule.setEnabled(enabled);
    if (severity != null && !severity.isBlank()) rule.setSeverity(severity);
    return repo.save(rule);
  }
}
