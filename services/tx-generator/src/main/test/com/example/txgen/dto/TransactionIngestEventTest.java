package com.example.txgen.dto;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TransactionIngestEventTest {

    @RepeatedTest(10)
    void randomProducesReasonableValues() {
        TransactionIngestEvent event = TransactionIngestEvent.random();

        // id
        assertNotNull(event.transaction_id());
        assertFalse(event.transaction_id().isBlank(), "transaction_id must not be blank");

        // account_id: [1, 100]
        assertTrue(event.account_id() >= 1 && event.account_id() <= 100,
                "account_id must be in [1, 100]");

        // amount
        BigDecimal amount = event.amount();
        assertNotNull(amount);
        assertEquals(2, amount.scale(), "amount scale must be 2");
        assertTrue(amount.compareTo(BigDecimal.TEN) >= 0, "amount >= 10");

        // верхнюю границу оценим с запасом
        BigDecimal max = new BigDecimal("25010.00");
        assertTrue(amount.compareTo(max) < 0, "amount must be < 25010.00");

        // currency
        Set<String> currencies = Set.of("RUB", "USD", "EUR");
        assertTrue(currencies.contains(event.currency()), "unexpected currency: " + event.currency());

        // country
        Set<String> countries = Set.of("RU", "US", "DE", "NL", "TR", "AE", "GB", "CA");
        assertTrue(countries.contains(event.country()), "unexpected country: " + event.country());

        // merchant
        Set<String> merchants = Set.of("...", "AMAZON", "STEAM", "MEGAMARKET", "APPLE", "GOOGLE", "UBER", "NETFLIX");
        assertTrue(merchants.contains(event.merchant()), "unexpected merchant: " + event.merchant());

        // source
        assertEquals("kafka", event.source());
    }

    @Test
    void recordComponentsEqualityHashCodeAndToString() {
        TransactionIngestEvent e1 = TransactionIngestEvent.random();
        TransactionIngestEvent e2 = new TransactionIngestEvent(
                e1.transaction_id(),
                e1.account_id(),
                e1.amount(),
                e1.currency(),
                e1.country(),
                e1.merchant(),
                e1.source()
        );

        // record генерит equals/hashCode, потрогаем их
        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertTrue(e1.toString().contains("transaction_id"));
    }
}
