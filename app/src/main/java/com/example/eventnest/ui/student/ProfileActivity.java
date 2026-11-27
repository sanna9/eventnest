package com.example.eventnest.ui.student;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventnest.R;
import com.example.eventnest.data.database.AppDatabase;
import com.example.eventnest.data.entity.User;
import com.example.eventnest.data.model.Role;
import com.example.eventnest.databinding.ActivityProfileBinding;
import com.example.eventnest.ui.auth.LoginActivity;
import com.example.eventnest.util.AppExecutors;
import com.example.eventnest.util.PasswordUtils;
import com.example.eventnest.util.SessionManager;
import com.example.eventnest.util.UiUtils;
import com.example.eventnest.util.ValidationUtils;

import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    // for easier error handling
    private enum PasswordResult { OK, WRONG_CURRENT, FAILED }
    private ActivityProfileBinding binding;
    private AppDatabase db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        session = new SessionManager(this);
        // Only a signed-in student may see this screen.
        if (!session.isLoggedIn() || session.getRole() != Role.STUDENT) {
            returnToLogin();
            return;
        }

        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            UiUtils.applySystemBarPadding(binding.getRoot());
        }

        db = AppDatabase.getInstance(this);

        binding.buttonBack.setOnClickListener(v -> finish());
        binding.buttonSaveName.setOnClickListener(v -> saveName());
        binding.buttonChangePassword.setOnClickListener(v -> changePassword());
        binding.buttonLogout.setOnClickListener(v -> {
            session.logout();
            returnToLogin();
        });

        loadUser();
    }

    private void loadUser() {
        int userId = session.getUserId();

        AppExecutors.run(() -> {
            try {
                User user = db.userDao().getUserByID(userId);
                AppExecutors.runOnMain(() -> showUser(user));
            } catch (Exception e) {
                AppExecutors.runOnMain(() -> showMessage(R.string.error_database_operation));
            }
        });
    }

    private void showUser(User user) {
        if (isFinishing() || isDestroyed()) return;

        // account no longer exists
        if (user == null) {
            session.logout();
            returnToLogin();
            return;
        }

        binding.textEmail.setText(user.getEmail());
        binding.inputName.setText(user.getName());
        showName(user.getName());
    }

    /** Updates the name and the avatar letters on the screen. */
    private void showName(String name) {
        binding.textName.setText(name);
        binding.textAvatar.setText(getAvatarByInitialName(name));
    }

    /**
     * Extracts up to two uppercase initials from a full name for use in a letter avatar
     * @param fullName full name of the user
     * @return 1 or 2 uppercase initials, or "?" when empty
     */
    private static String getAvatarByInitialName(String fullName) {
        // check null
        if (fullName == null) {
            return "?";
        }
        // find the first non-whitespace character (start of the first word)
        int len = fullName.length();
        int firstIndex = -1;
        for (int i = 0; i < len; i++) {
            if (!Character.isWhitespace(fullName.charAt(i))) {
                firstIndex = i;
                break;
            }
        }

        if (firstIndex == -1) {
            return "?";
        }
        // skip trailing spaces from the end of the string
        int lastEnd = len - 1;
        while (lastEnd > firstIndex && Character.isWhitespace(fullName.charAt(lastEnd))) {
            lastEnd--;
        }
        // move backward to find the first character of the last word
        int lastStart = lastEnd;
        while (lastStart > firstIndex && !Character.isWhitespace(fullName.charAt(lastStart - 1))) {
            lastStart--;
        }
        // only one word exists if the last word starts at the first character
        if (lastStart == firstIndex) {
            return String.valueOf(fullName.charAt(firstIndex)).toUpperCase(Locale.ROOT);
        }

        String initials = "" + fullName.charAt(firstIndex) + fullName.charAt(lastStart);
        return initials.toUpperCase(Locale.ROOT);
    }

    private void saveName() {
        binding.layoutName.setError(null);

        String name = ValidationUtils.normalizeName(text(binding.inputName.getText()));
        if (name.isEmpty()) {
            binding.layoutName.setError(getString(R.string.error_name_required));
            return;
        }

        binding.buttonSaveName.setEnabled(false);
        int userId = session.getUserId();

        AppExecutors.run(() -> {
            try {
                int rows = db.userDao().updateName(userId, name);

                AppExecutors.runOnMain(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    binding.buttonSaveName.setEnabled(true);

                    if (rows == 1) {
                        // shows the clean version
                        binding.inputName.setText(name);
                        showName(name);
                        showMessage(R.string.toast_name_updated);
                    } else {
                        showMessage(R.string.error_database_operation);
                    }
                });
            } catch (Exception e) {
                AppExecutors.runOnMain(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    binding.buttonSaveName.setEnabled(true);
                    showMessage(R.string.error_database_operation);
                });
            }
        });
    }

    private void changePassword() {
        binding.layoutCurrentPassword.setError(null);
        binding.layoutNewPassword.setError(null);
        binding.layoutConfirmPassword.setError(null);

        String current = text(binding.inputCurrentPassword.getText());
        String newPassword = text(binding.inputNewPassword.getText());
        String confirm = text(binding.inputConfirmPassword.getText());

        // password validation
        boolean valid = true;
        if (current.isEmpty()) {
            binding.layoutCurrentPassword.setError(getString(R.string.error_password_required));
            valid = false;
        }
        if (!ValidationUtils.isValidPassword(newPassword)) {
            binding.layoutNewPassword.setError(getString(
                    R.string.error_password_short, ValidationUtils.MIN_PASSWORD_LENGTH));
            valid = false;
        } else if (newPassword.equals(current)) {
            binding.layoutNewPassword.setError(getString(R.string.error_password_same));
            valid = false;
        } else if (!newPassword.equals(confirm)) {
            binding.layoutConfirmPassword.setError(getString(R.string.error_password_mismatch));
            valid = false;
        }
        if (!valid) return;

        setPasswordLoading(true);
        int userId = session.getUserId();

        AppExecutors.run(() -> {
            PasswordResult result;
            try {
                // Read the saved hash fresh from the database.
                User user = db.userDao().getUserByID(userId);

                if (user == null) {
                    result = PasswordResult.FAILED;
                } else if (!PasswordUtils.verifyPassword(current, user.getPasswordHash())) {
                    result = PasswordResult.WRONG_CURRENT;
                } else {
                    String newHash = PasswordUtils.hashPassword(newPassword);
                    int rows = db.userDao().updatePasswordHash(userId, newHash);
                    result = (rows == 1) ? PasswordResult.OK : PasswordResult.FAILED;
                }
            } catch (Exception e) {
                result = PasswordResult.FAILED;
            }

            PasswordResult finalResult = result;
            AppExecutors.runOnMain(() -> onPasswordResult(finalResult));
        });
    }

    private void onPasswordResult(PasswordResult result) {
        if (isFinishing() || isDestroyed()) return;
        setPasswordLoading(false);

        switch (result) {
            case OK:
                showMessage(R.string.toast_password_updated);
                binding.inputCurrentPassword.setText("");
                binding.inputNewPassword.setText("");
                binding.inputConfirmPassword.setText("");
                binding.inputConfirmPassword.clearFocus();
                break;
            case WRONG_CURRENT:
                binding.layoutCurrentPassword.setError(
                        getString(R.string.error_current_password_wrong));
                break;
            default:
                showMessage(R.string.error_database_operation);
        }
    }

    private void setPasswordLoading(boolean loading) {
        // Disable button and switch label while password update runs in background
        // prevent duplicate submits and show progress
        binding.buttonChangePassword.setEnabled(!loading);
        binding.buttonChangePassword.setText(
                loading ? R.string.updating : R.string.button_update_password);
    }

    private void returnToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showMessage(int stringResource) {
        Toast.makeText(this, stringResource, Toast.LENGTH_SHORT).show();
    }

    private static String text(CharSequence value) {
        return value == null ? "" : value.toString();
    }
}
