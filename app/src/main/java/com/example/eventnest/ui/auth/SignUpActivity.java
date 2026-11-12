package com.example.eventnest.ui.auth;

import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Build;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventnest.R;
import com.example.eventnest.data.database.AppDatabase;
import com.example.eventnest.data.entity.User;
import com.example.eventnest.data.model.Role;
import com.example.eventnest.databinding.ActivitySignUpBinding;
import com.example.eventnest.util.AppExecutors;
import com.example.eventnest.util.PasswordUtils;
import com.example.eventnest.util.UiUtils;
import com.example.eventnest.util.ValidationUtils;

public class SignUpActivity extends AppCompatActivity {

    // Returned to LoginActivity so it can pre-fill the email field.
    public static final String EXTRA_EMAIL = "extra_email";
    private enum SignUpResult { OK, EMAIL_TAKEN, FAILED }
    ActivitySignUpBinding binding;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            UiUtils.applySystemBarPadding(binding.getRoot());
        }
        db = AppDatabase.getInstance(this);

        binding.buttonCreateAccount.setOnClickListener(v -> attemptSignUp());
        // TextInputEditText listens to the keyboard, and it attaches with setOnEditorActionListener
        // instead of setOnClickListener
        binding.inputConfirmPassword.setOnEditorActionListener((v, actionId, event) -> {
            // EditorInfo.IME_ACTION_DONE will check whether user has pressed the checkmark or "Done" key
            // IME also stand for Input Method Editor FYI
            // @see https://developer.android.com/reference/android/view/inputmethod/EditorInfo
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptSignUp();
                return true;
            }
            return false;
        });
        binding.textBackToSignIn.setOnClickListener(v -> finish());
    }

    /**
     * @author Hung Le
     * I'm currently using TextInputLayout for a better look
     * and also it has some validation handling UI like setError().
     */
    private void attemptSignUp() {
        clearErrors();

        String name = ValidationUtils.normalizeName(text(binding.inputName.getText()));
        String email = ValidationUtils.normalizeEmail(text(binding.inputEmail.getText()));
        String password = text(binding.inputPassword.getText());
        String confirm = text(binding.inputConfirmPassword.getText());

        boolean valid = true;
        if (name.isEmpty()) {
            binding.layoutName.setError(getString(R.string.error_name_required));
            valid = false;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            binding.layoutEmail.setError(getString(R.string.error_email_invalid));
            valid = false;
        }
        if (!ValidationUtils.isValidPassword(password)) {
            binding.layoutPassword.setError(getString(
                    R.string.error_password_short, ValidationUtils.MIN_PASSWORD_LENGTH));
            valid = false;
        } else if (!password.equals(confirm)) {
            binding.layoutConfirmPassword.setError(getString(R.string.error_password_mismatch));
            valid = false;
        }
        if (!valid) return;

        // puts the button to loading state
        setLoading(true);

        // run() runs on background thread
        AppExecutors.run(() -> {
            SignUpResult result;
            try {
                if (db.userDao().getUserByEmail(email) != null) {
                    // Deactivated account's email stays reserved.
                    result = SignUpResult.EMAIL_TAKEN;
                } else {
                    User user = new User();
                    user.setName(name);
                    user.setEmail(email);
                    user.setPasswordHash(PasswordUtils.hashPassword(password));
                    // sign up is for student only
                    user.setRole(Role.STUDENT);
                    user.setActive(true);
                    db.userDao().addUser(user);
                    result = SignUpResult.OK;
                }
            } catch (SQLiteConstraintException e) {
                // The unique index on Email caught a duplicate we did not see first.
                result = SignUpResult.EMAIL_TAKEN;
            } catch (RuntimeException e) {
                result = SignUpResult.FAILED;
            }

            SignUpResult finalResult = result;
            AppExecutors.runOnMain(() -> onSignUpResult(finalResult, email));
        });
    }

    private void onSignUpResult(SignUpResult result, String email) {
        if (isFinishing() || isDestroyed()) return;
        // after finished sign up no more loading
        setLoading(false);
        switch (result) {
            // Success
            case OK:
                Toast.makeText(this, R.string.toast_account_created, Toast.LENGTH_LONG).show();
                setResult(RESULT_OK, new Intent().putExtra(EXTRA_EMAIL, email));
                finish();
                break;
            //
            case EMAIL_TAKEN:
                binding.layoutEmail.setError(getString(R.string.error_email_taken));
                break;
            default:
                Toast.makeText(this, R.string.error_signup_failed, Toast.LENGTH_LONG).show();
        }
    }

    private void clearErrors() {
        binding.layoutName.setError(null);
        binding.layoutEmail.setError(null);
        binding.layoutPassword.setError(null);
        binding.layoutConfirmPassword.setError(null);
    }

    /**
     *
     * @param loading state for the button,
     *                if it's true the loading button will be set to loading state
     */
    private void setLoading(boolean loading) {
        binding.buttonCreateAccount.setEnabled(!loading);
        binding.buttonCreateAccount.setText(
                loading ? R.string.creating_account : R.string.button_create_account);
    }

    /**
     * @author Hung Le
     * @param value The text from viewbinding's EditText
     * @return toString of the getText's output and avoiding null pointer exception
     */
    private static String text(CharSequence value) {
        return value == null ? "" : value.toString();
    }
}