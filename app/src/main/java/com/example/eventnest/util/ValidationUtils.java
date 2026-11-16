package com.example.eventnest.util;

import android.util.Patterns;

import java.util.Locale;

public final class ValidationUtils {
    public static final int MIN_PASSWORD_LENGTH = 6;
    public ValidationUtils() {
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    // Trim and collapse repeated spaces on user's name
    public static String normalizeName(String name) {
        return name == null ? "" : name.trim().replaceAll("\\s+", " ");
    }

    /**
     * Using Patterns from java
     * @param email from user's sign up input
     * @return boolean to check if the input is valid or not
     *
     * @see <a href="https://developer.android.com/reference/android/util/Patterns#EMAIL_ADDRESS">Email patterns from java</a>
     */
    public static boolean isValidEmail(String email) {
        return email != null && !email.isEmpty()
                && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }
}
