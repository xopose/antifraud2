package com.example.txgen;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorRunnerTest {

    private static int invokeParsePositiveInt(String value, int def) throws Exception {
        Method m = GeneratorRunner.class.getDeclaredMethod("parsePositiveInt", String.class, int.class);
        m.setAccessible(true);
        return (int) m.invoke(null, value, def);
    }

    @Test
    void parsePositiveIntParsesValidValue() throws Exception {
        int result = invokeParsePositiveInt("42", 5);
        assertEquals(42, result);
    }

    @Test
    void parsePositiveIntReturnsDefaultOnNullBlankNegativeOrInvalid() throws Exception {
        assertEquals(10, invokeParsePositiveInt(null, 10));
        assertEquals(10, invokeParsePositiveInt("   ", 10));
        assertEquals(10, invokeParsePositiveInt("-1", 10));
        assertEquals(10, invokeParsePositiveInt("not_a_number", 10));
    }
}
