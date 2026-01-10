package com.example.antifraud.controller;

import com.example.antifraud.entity.FraudRuleEntity;
import com.example.antifraud.service.RuleService;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RuleControllerTest {

    @Test
    @SuppressWarnings("unchecked")
    void updatePassesParsedValuesToServiceAndBuildsResponseMap() {
        RuleService service = mock(RuleService.class);

        FraudRuleEntity rule = new FraudRuleEntity();
        rule.setThreshold(200.0);
        rule.setEnabled(true);
        rule.setSeverity("HIGH");

        // Чтобы getCode() что-то вернуло — через reflection положим значение в поле code
        try {
            var codeField = FraudRuleEntity.class.getDeclaredField("code");
            codeField.setAccessible(true);
            codeField.set(rule, "R1");
        } catch (Exception e) {
            fail("Failed to set code via reflection: " + e.getMessage());
        }

        when(service.update("R1", 100.0, Boolean.FALSE, "MEDIUM")).thenReturn(rule);

        RuleController controller = new RuleController(service);

        Map<String, Object> body = Map.of(
                "threshold", 100.0,
                "enabled", false,
                "severity", "MEDIUM"
        );

        Object resultObj = controller.update("R1", body);
        assertTrue(resultObj instanceof Map);

        Map<String, Object> result = (Map<String, Object>) resultObj;

        assertEquals("R1", result.get("code"));
        assertEquals(200.0, result.get("threshold"));
        assertEquals(true, result.get("enabled"));
        assertEquals("HIGH", result.get("severity"));

        verify(service).update("R1", 100.0, Boolean.FALSE, "MEDIUM");
    }
}
