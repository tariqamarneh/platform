package com.businessagent.auth.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiKeyGeneratorTest {

    @Test
    void generate_shouldReturnStringStartingWithPrefix() {
        String key = ApiKeyGenerator.generate();

        assertNotNull(key);
        assertTrue(key.startsWith("ba_live_"));
    }

    @Test
    void generate_shouldReturnUniqueValuesOnMultipleCalls() {
        Set<String> keys = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            keys.add(ApiKeyGenerator.generate());
        }

        assertEquals(100, keys.size());
    }

    @Test
    void hash_shouldReturnConsistentHashForSameInput() {
        String input = "ba_live_test_key_12345";

        String hash1 = ApiKeyGenerator.hash(input);
        String hash2 = ApiKeyGenerator.hash(input);

        assertEquals(hash1, hash2);
    }

    @Test
    void hash_shouldReturnDifferentHashForDifferentInput() {
        String hash1 = ApiKeyGenerator.hash("input_one");
        String hash2 = ApiKeyGenerator.hash("input_two");

        assertNotEquals(hash1, hash2);
    }

    @Test
    void hash_shouldReturn64CharHexString() {
        String hash = ApiKeyGenerator.hash("some_key");

        assertNotNull(hash);
        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]{64}"));
    }

    @Test
    void prefix_shouldReturnFirst12Characters() {
        String key = "ba_live_abcdef123456789";

        String prefix = ApiKeyGenerator.prefix(key);

        assertEquals("ba_live_abcd", prefix);
        assertEquals(12, prefix.length());
    }
}
