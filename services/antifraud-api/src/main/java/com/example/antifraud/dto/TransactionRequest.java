package com.example.antifraud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionRequest(
    @NotBlank String transaction_id,
    @NotNull Long account_id,
    @NotNull BigDecimal amount,
    @NotBlank String currency,
    @NotBlank String country,
    @NotBlank String merchant
) {}
