package com.example.antifraud.repo;

import com.example.antifraud.entity.FraudAlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudAlertRepository extends JpaRepository<FraudAlertEntity, Long> {
}
