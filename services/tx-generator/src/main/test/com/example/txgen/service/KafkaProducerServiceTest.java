package com.example.txgen.service;

import com.example.txgen.dto.TransactionIngestEvent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class KafkaProducerServiceTest {

    @Test
    void getenvOrDefaultReturnsDefaultWhenMissing() throws Exception {
        Method m = KafkaProducerService.class
                .getDeclaredMethod("getenvOrDefault", String.class, String.class);
        m.setAccessible(true);

        String value = (String) m.invoke(null, "__ENV_KEY_DOES_NOT_EXIST__", "fallback");
        assertEquals("fallback", value);
    }

    @Test
    void getenvFirstNonBlankReturnsNullWhenAllMissing() throws Exception {
        Method m = KafkaProducerService.class
                .getDeclaredMethod("getenvFirstNonBlank", String[].class);
        m.setAccessible(true);

        String[] keys = new String[]{"__ENV_ONE__", "__ENV_TWO__"};
        String value = (String) m.invoke(null, (Object) keys);
        assertNull(value);
    }

    /**
     * Тест, который:
     * - если bootstrap-переменные НЕ заданы — проверяет, что конструктор кидает IllegalStateException;
     * - если заданы — создаёт реальный продьюсер, шлёт одно сообщение и закрывает его.
     *
     * Так тест будет работать и в dev-окружении без Kafka, и в CI, где ты можешь передать
     * KAFKA_BOOTSTRAP/SPRING_KAFKA_BOOTSTRAP_SERVERS.
     */
    @Test
    void constructorAndSendBehaviourDependsOnEnvironment() {
        String bootstrap = System.getenv("SPRING_KAFKA_BOOTSTRAP_SERVERS");
        if (bootstrap == null || bootstrap.isBlank()) {
            bootstrap = System.getenv("KAFKA_BOOTSTRAP");
        }

        if (bootstrap == null || bootstrap.isBlank()) {
            // Бутстрап не задан — проверяем ветку с исключением
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    KafkaProducerService::new
            );
            assertTrue(ex.getMessage().toLowerCase().contains("kafka"));
        } else {
            // Бутстрап задан — проверяем happy path: конструктор, send, destroy
            KafkaProducerService service = new KafkaProducerService();
            assertNotNull(service);

            // Проверяем, что можно отправить одно событие (Kafka-подключения не требуется синхронно)
            service.send(TransactionIngestEvent.random());

            // Проверяем, что destroy() не кидает исключений
            assertDoesNotThrow(service::destroy);
        }
    }
}
