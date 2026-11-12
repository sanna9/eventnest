package com.example.eventnest.ui.auth;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventnest.R;
import com.example.eventnest.data.database.AppDatabase;
import com.example.eventnest.data.entity.User;
import com.example.eventnest.data.model.Role;
import com.example.eventnest.databinding.ActivityLoginBinding;
import com.example.eventnest.ui.organizer.OrganizerDashboardActivity;
import com.example.eventnest.util.AppExecutors;
import com.example.eventnest.util.DatabaseSeeder;
import com.example.eventnest.util.PasswordUtils;
import com.example.eventnest.util.SessionManager;
import com.example.eventnest.util.UiUtils;
import com.example.eventnest.util.ValidationUtils;

public class LoginActivity extends AppCompatActivity {

    private enum LoginResult {OK, INVALID, DEACTIVATED}

    private ActivityLoginBinding binding;
    private AppDatabase db;
    private SessionManager session;

    /**
     *
     */
    private final ActivityResultLauncher<Intent> signUpLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String email = result.getData().getStringExtra(SignUpActivity.EXTRA_EMAIL);
                    if (email != null) {
                        binding.inputEmail.setText(email);
                        binding.inputPassword.requestFocus();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        session = new SessionManager(this);
        // TODO: re-enable when the dashboards exist.
        //        if (session.isLoggedIn()) {
        //            openDashboard(session.getRole());
        //            return;
        //        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Requires SDK 29+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            UiUtils.applySystemBarPadding(binding.getRoot());
        }

        db = AppDatabase.getInstance(this);
        // Init pre-created accounts
        AppExecutors.run(() -> {
            DatabaseSeeder.seed(db);
        });

        binding.buttonLogin.setOnClickListener(v -> attemptLogin());
        // Testing setOnEditorActionListener
        // Main purpose here's when user click done system will proceed next step instead of hiding keyboard
        binding.inputPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });
        binding.textSignUp.setOnClickListener(v ->
                signUpLauncher.launch(new Intent(this, SignUpActivity.class)));
    }

    private void attemptLogin() {
        binding.layoutEmail.setError(null);
        binding.layoutPassword.setError(null);
        binding.textError.setVisibility(android.view.View.GONE);

        String email = ValidationUtils.normalizeEmail(
                String.valueOf(binding.inputEmail.getText()));
        // Spaces may be part of password, so I'm not trimming it here
        String password = String.valueOf(binding.inputPassword.getText());

        boolean valid = true;
        if (email.isEmpty()) {
            binding.layoutEmail.setError(getString(R.string.error_email_required));
            valid = false;
        }
        if (password.isEmpty()) {
            binding.layoutPassword.setError(getString(R.string.error_password_required));
            valid = false;
        }
        if (!valid) return;

        setLoading(true);
        AppExecutors.run(() -> {
            User user = db.userDao().getUserByEmail(email);

            LoginResult result;
            if (user == null
                    || user.getRole() == null
                    || !PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
                // Same message for "no such email" and "wrong password".
                result = LoginResult.INVALID;
            } else if (!user.isActive()) {
                // Only revealed after the password is correct.
                result = LoginResult.DEACTIVATED;
            } else {
                // Saved here, so the login is kept even if the screen is recreated.
                session.login(user.getUserID(), user.getRole().toString());
                result = LoginResult.OK;
            }

            Role role = (user == null) ? null : user.getRole();
            LoginResult finalResult = result;
            AppExecutors.runOnMain(() -> onLoginResult(finalResult, role));
        });
    }

    /**
     * Handles the login result and navigates the user also displays the error messages
     *
     * @param result The result of the authentication attempt (OK, DEACTIVATED, or INVALID)
     * @param role   The role of the user for routing if login succeeds
     */
    private void onLoginResult(LoginResult result, Role role) {
        // Prevent executing UI updates if the activity is already finishing or destroyed
        if (isFinishing() || isDestroyed()) return;
        setLoading(false);
        switch (result) {
            case OK:
                openDashboard(role);
                break;
            case DEACTIVATED:
                showError(getString(R.string.error_account_deactivated));
                break;
            default:
                showError(getString(R.string.error_invalid_credentials));
        }
    }

    /**
     * Displays an error message by updating the error text view visibility and content
     *
     * @param message The error message
     */
    private void showError(String message) {
        binding.textError.setText(message);
        binding.textError.setVisibility(android.view.View.VISIBLE);
    }

    /**
     * Updates the UI state of the login button during the authentication process
     * avoid user spam clicking and halt the performance
     *
     * @param loading True if the login request is in progress (disables the button
     *                and changes the text to "Signing in..."), false otherwise
     */
    private void setLoading(boolean loading) {
        binding.buttonLogin.setEnabled(!loading);
        binding.buttonLogin.setText(loading ? R.string.signing_in : R.string.button_sign_in);
    }

    private void openDashboard(Role role) {
        if (role == Role.ORGANIZER) {
            Intent intent = new Intent(
                    this,
                    OrganizerDashboardActivity.class
            );

            // Clear previous screens so Back cannot return to login.
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();

        } else if (role == Role.STUDENT) {
            // Modify this section for Dashboard Student later
            // TODO: startActivity(new Intent(this, MainActivity.class));
            Toast.makeText(this, "logged as student", Toast.LENGTH_SHORT).show();
        }
        // TODO: re-enable when the dashboards exist.
        //        finish();
    }
}