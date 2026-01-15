package com.example.antifraud.repo;

import com.example.antifraud.entity.FraudRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FraudRuleRepository extends JpaRepository<FraudRuleEntity, Integer> {
  Optional<FraudRuleEntity> findByCode(String code);
}
