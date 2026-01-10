package com.example.antifraud.service;

import com.example.antifraud.entity.FraudRuleEntity;
import com.example.antifraud.repo.FraudRuleRepository;
import com.example.antifraud.repo.TransactionRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FraudEngineTest {

    private FraudEngine createEngine(FraudRuleRepository ruleRepo) {
        TransactionRepository txRepo = mock(TransactionRepository.class);
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        return new FraudEngine(ruleRepo, txRepo, registry);
    }

    @Test
    void normalizeCountryTrimsAndUppercases() throws Exception {
        FraudEngine engine = createEngine(mock(FraudRuleRepository.class));

        Method m = FraudEngine.class.getDeclaredMethod("normalizeCountry", String.class);
        m.setAccessible(true);

        String nullCountry = (String) m.invoke(engine, (Object) null);
        String trimmed = (String) m.invoke(engine, "  ru  ");

        assertEquals("", nullCountry);
        assertEquals("RU", trimmed);
    }

    @Test
    @SuppressWarnings("unchecked")
    void loadRulesBuildsMapByCode() throws Exception {
        FraudRuleRepository repo = mock(FraudRuleRepository.class);

        FraudRuleEntity r1 = new FraudRuleEntity();
        FraudRuleEntity r2 = new FraudRuleEntity();

        // Проставляем код через reflection, т.к. сеттера нет
        Field codeField = FraudRuleEntity.class.getDeclaredField("code");
        codeField.setAccessible(true);
        codeField.set(r1, "RULE1");
        codeField.set(r2, "RULE2");

        when(repo.findAll()).thenReturn(List.of(r1, r2));

        FraudEngine engine = createEngine(repo);

        Method m = FraudEngine.class.getDeclaredMethod("loadRules");
        m.setAccessible(true);

        Map<String, FraudRuleEntity> map = (Map<String, FraudRuleEntity>) m.invoke(engine);

        assertEquals(2, map.size());
        assertSame(r1, map.get("RULE1"));
        assertSame(r2, map.get("RULE2"));

        verify(repo).findAll();
    }
}
