package com.example.eventnest.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.eventnest.data.model.Role;

public class SessionManager {
    private static final String PREFS = "eventnest_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_ROLE = "role";
    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void login(int userId, String role) {
        prefs.edit().putInt(KEY_USER_ID, userId).putString(KEY_ROLE, role).apply();
    }

    public boolean isLoggedIn() {
        return prefs.contains(KEY_USER_ID);
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public Role getRole() {
        String name = prefs.getString(KEY_ROLE, null);
        if (name == null) return null;
        try {
            return Role.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void logout() {
        prefs.edit().clear().apply();
    }
}
