package com.example.eventnest.util;

import java.util.Locale;

public final class ValidationUtils {
    public ValidationUtils() {
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
