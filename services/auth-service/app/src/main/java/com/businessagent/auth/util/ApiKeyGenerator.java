package com.businessagent.auth.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

public final class ApiKeyGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private ApiKeyGenerator() {
        // utility class
    }

    public static String generate() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return "ba_live_" + HexFormat.of().formatHex(bytes);
    }

    public static String hash(String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    public static String prefix(String rawKey) {
        if (rawKey == null || rawKey.length() < 12) {
            throw new IllegalArgumentException("Raw key must be at least 12 characters");
        }
        return rawKey.substring(0, 12);
    }
}
