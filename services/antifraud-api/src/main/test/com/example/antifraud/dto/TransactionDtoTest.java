package com.example.antifraud.dto;

import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionDtoTest {

    @Test
    void transactionRequestHoldsProvidedValues() {
        TransactionRequest req = new TransactionRequest(
                "tx-1",
                42L,
                new BigDecimal("123.45"),
                "RUB",
                "RU",
                "SHOP"
        );

        assertEquals("tx-1", req.transaction_id());
        assertEquals(42L, req.account_id());
        assertEquals(new BigDecimal("123.45"), req.amount());
        assertEquals("RUB", req.currency());
        assertEquals("RU", req.country());
        assertEquals("SHOP", req.merchant());
    }

    @Test
    void transactionResponseHoldsProvidedValues() {
        TransactionResponse resp = new TransactionResponse(
                "APPROVED",
                List.of("RULE1", "RULE2")
        );

        assertEquals("APPROVED", resp.result());
        assertEquals(List.of("RULE1", "RULE2"), resp.rules_triggered());
    }

    @Test
    void transactionIngestEventHoldsProvidedValues() {
        TransactionIngestEvent event = new TransactionIngestEvent(
                "tx-2",
                7L,
                new BigDecimal("10.00"),
                "USD",
                "US",
                "STORE",
                "api"
        );

        assertEquals("tx-2", event.transaction_id());
        assertEquals(7L, event.account_id());
        assertEquals(new BigDecimal("10.00"), event.amount());
        assertEquals("USD", event.currency());
        assertEquals("US", event.country());
        assertEquals("STORE", event.merchant());
        assertEquals("api", event.source());
    }
}
