package com.example.antifraud.controller;

import com.example.antifraud.service.RuleService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class RuleController {
  private final RuleService service;

  public RuleController(RuleService service) {
    this.service = service;
  }

  @GetMapping("/rules")
  public Object all() {
    return service.all().stream().map(r -> Map.of(
        "code", r.getCode(),
        "title", r.getTitle(),
        "description", r.getDescription(),
        "threshold", r.getThreshold(),
        "enabled", r.isEnabled(),
        "severity", r.getSeverity()
    )).toList();
  }

  @PutMapping("/rules/{code}")
  public Object update(@PathVariable String code, @RequestBody Map<String, Object> body) {
    Double threshold = body.get("threshold") == null ? null : Double.valueOf(body.get("threshold").toString());
    Boolean enabled = body.get("enabled") == null ? null : Boolean.valueOf(body.get("enabled").toString());
    String severity = body.get("severity") == null ? null : body.get("severity").toString();
    var r = service.update(code, threshold, enabled, severity);
    return Map.of(
        "code", r.getCode(),
        "threshold", r.getThreshold(),
        "enabled", r.isEnabled(),
        "severity", r.getSeverity()
    );
  }
}
