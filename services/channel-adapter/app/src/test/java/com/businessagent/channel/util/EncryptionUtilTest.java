package com.businessagent.channel.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionUtilTest {

    private static final String KEY = "test-encryption-key-32-bytes-ok!";

    @Test
    void encryptThenDecrypt_shouldReturnOriginalPlaintext() {
        String plaintext = "my-secret-api-key-12345";

        String ciphertext = EncryptionUtil.encrypt(plaintext, KEY);
        String decrypted = EncryptionUtil.decrypt(ciphertext, KEY);

        assertEquals(plaintext, decrypted);
    }

    @Test
    void encrypt_differentPlaintexts_shouldProduceDifferentCiphertexts() {
        String ciphertext1 = EncryptionUtil.encrypt("plaintext-one", KEY);
        String ciphertext2 = EncryptionUtil.encrypt("plaintext-two", KEY);

        assertNotEquals(ciphertext1, ciphertext2);
    }

    @Test
    void decrypt_withWrongKey_shouldThrowRuntimeException() {
        String ciphertext = EncryptionUtil.encrypt("secret", KEY);

        assertThrows(RuntimeException.class,
                () -> EncryptionUtil.decrypt(ciphertext, "wrong-key-that-is-also-32-bytes!"));
    }

    @Test
    void encrypt_withNull_shouldThrowException() {
        assertThrows(RuntimeException.class,
                () -> EncryptionUtil.encrypt(null, KEY));
    }
}
