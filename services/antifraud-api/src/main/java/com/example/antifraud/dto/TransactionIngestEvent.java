package com.example.antifraud.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionIngestEvent(
    String transaction_id,
    Long account_id,
    BigDecimal amount,
    String currency,
    String country,
    String merchant,
    String source
) {}
