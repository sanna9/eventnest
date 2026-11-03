package com.example.eventnest.util;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordUtils {
    public static String hashPassword(String password) {

        // random salt for this account.
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        String hash = calculateHash(password, salt);
        //saving salt +hash
        return Base64.encodeToString(salt, Base64.NO_WRAP)
                + ":" + hash;
    }

    //used by shared login screen
    public static boolean verifyPassword(
            String enteredPassword,
            String storedPasswordHash
    ) {
        if (storedPasswordHash == null) {
            return false;
        }

        String[] parts = storedPasswordHash.split(":");

        if (parts.length != 2) {
            return false;
        }

        try {
           //fetching salt when account is created
            byte[] salt = Base64.decode(parts[0], Base64.NO_WRAP);
            String enteredHash = calculateHash(enteredPassword, salt);
            // Comparing with stored hash
            return MessageDigest.isEqual(
                    enteredHash.getBytes(StandardCharsets.UTF_8),
                    parts[1].getBytes(StandardCharsets.UTF_8)
            );

        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    private static String calculateHash(String password, byte[] salt) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            digest.update(salt);

            byte[] hash = digest.digest(
                    password.getBytes(StandardCharsets.UTF_8)
            );

            return Base64.encodeToString(hash, Base64.NO_WRAP);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}