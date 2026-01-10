package com.example.antifraud.controller;

import com.example.antifraud.dto.TransactionIngestEvent;
import com.example.antifraud.dto.TransactionRequest;
import com.example.antifraud.dto.TransactionResponse;
import com.example.antifraud.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
public class TransactionController {

  private final TransactionService service;

  public TransactionController(TransactionService service) {
    this.service = service;
  }

  @PostMapping("/airflow_transactions")
  public TransactionResponse airflowLoad(@Valid @RequestBody TransactionRequest req) {
    var ev = new TransactionIngestEvent(
            req.transaction_id(),
            req.account_id(),
            req.amount(),
            req.currency(),
            req.country(),
            req.merchant(),
            "airflow"
    );
    TransactionIngestEvent payload = ev;
    return service.ingest(ev, payload);
  }

  @PostMapping("/transactions")
  public TransactionResponse addAndCheck(@Valid @RequestBody TransactionRequest req) {
    var ev = new TransactionIngestEvent(
        req.transaction_id(),
        req.account_id(),
        req.amount(),
        req.currency(),
        req.country(),
        req.merchant(),
        "http"
    );
    TransactionIngestEvent payload = ev;
    return service.ingest(ev, payload);
  }

  @GetMapping("/transactions")
  public Object search(
      @RequestParam(name="id", required=false) String transactionId,
      @RequestParam(name="account_id", required=false) Long accountId,
      @RequestParam(name="status", required=false) String status,
      @RequestParam(name="from", required=false) Instant from,
      @RequestParam(name="to", required=false) Instant to
  ) {
    return service.search(transactionId, accountId, status, from, to)
        .stream()
        .map(t -> Map.of(
            "transaction_id", t.getTransactionId(),
            "created_at", t.getCreatedAt(),
            "account_id", t.getAccountId(),
            "amount", t.getAmount(),
            "currency", t.getCurrency(),
            "country", t.getCountry(),
            "merchant", t.getMerchant(),
            "status", t.getStatus(),
            "source", t.getSource(),
            "ingested_at", t.getIngestedAt()
        ))
        .toList();
  }
}
