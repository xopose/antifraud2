package com.example.antifraud.service;

import com.example.antifraud.entity.FraudRuleEntity;
import com.example.antifraud.repo.FraudRuleRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RuleServiceTest {

    @Test
    void allDelegatesToRepository() {
        FraudRuleRepository repo = mock(FraudRuleRepository.class);
        FraudRuleEntity r1 = new FraudRuleEntity();
        FraudRuleEntity r2 = new FraudRuleEntity();
        when(repo.findAll()).thenReturn(List.of(r1, r2));

        RuleService service = new RuleService(repo);

        List<FraudRuleEntity> result = service.all();

        assertEquals(2, result.size());
        assertTrue(result.contains(r1));
        assertTrue(result.contains(r2));
        verify(repo).findAll();
    }

    @Test
    void updateUpdatesOnlyNonNullFields() {
        FraudRuleRepository repo = mock(FraudRuleRepository.class);
        FraudRuleEntity rule = new FraudRuleEntity();
        rule.setThreshold(100.0);
        rule.setEnabled(false);
        rule.setSeverity("LOW");

        when(repo.findByCode("R1")).thenReturn(Optional.of(rule));
        when(repo.save(any(FraudRuleEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        RuleService service = new RuleService(repo);

        FraudRuleEntity updated = service.update("R1", 200.0, true, "HIGH");

        assertEquals(200.0, updated.getThreshold());
        assertTrue(updated.isEnabled());
        assertEquals("HIGH", updated.getSeverity());

        verify(repo).findByCode("R1");
        verify(repo).save(rule);
    }

    @Test
    void updateThrowsIfRuleNotFound() {
        FraudRuleRepository repo = mock(FraudRuleRepository.class);
        when(repo.findByCode("UNKNOWN")).thenReturn(Optional.empty());

        RuleService service = new RuleService(repo);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.update("UNKNOWN", null, null, null)
        );

        assertTrue(ex.getMessage().contains("Rule not found"));
    }
}
