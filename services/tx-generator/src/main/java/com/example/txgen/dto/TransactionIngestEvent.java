package com.example.txgen.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public record TransactionIngestEvent(
        String transaction_id,
        long account_id,
        BigDecimal amount,
        String currency,
        String country,
        String merchant,
        String source
) {

    private static final Random RND = new Random();
    private static final List<String> CURRENCIES = List.of("RUB", "USD", "EUR");
    private static final List<String> COUNTRIES = List.of("RU", "US", "DE", "NL", "TR", "AE", "GB", "CA");
    private static final List<String> MERCHANTS = List.of("AMAZON", "STEAM", "MEGAMARKET", "APPLE", "GOOGLE", "UBER", "NETFLIX");

    public static TransactionIngestEvent random() {
        String currency = pick(CURRENCIES);
        String country = pick(COUNTRIES);
        String merchant = pick(MERCHANTS);

        long accountId = RND.nextInt(0, 99) + 1; //acc id 1:100
        double amt = 10 + RND.nextDouble() * 25_000;
        BigDecimal amount = BigDecimal.valueOf(amt).setScale(2, RoundingMode.HALF_UP);

        return new TransactionIngestEvent(
                UUID.randomUUID().toString(),
                accountId,
                amount,
                currency,
                country,
                merchant,
                "kafka"
        );
    }

    private static String pick(List<String> list) {
        return list.get(RND.nextInt(list.size()));
    }
}
