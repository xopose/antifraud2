package com.example.antifraud.dto;

import java.util.List;

public record TransactionResponse(
    String result,
    List<String> rules_triggered
) {}
