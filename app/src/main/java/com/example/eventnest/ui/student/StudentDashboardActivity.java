package com.example.eventnest.ui.student;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.eventnest.R;
import com.example.eventnest.data.model.Role;
import com.example.eventnest.databinding.ActivityStudentDashboardBinding;
import com.example.eventnest.ui.auth.LoginActivity;
import com.example.eventnest.util.SessionManager;
import com.example.eventnest.util.UiUtils;

public class StudentDashboardActivity extends AppCompatActivity {

    private ActivityStudentDashboardBinding binding;
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

        // Setup views
        binding = ActivityStudentDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            UiUtils.applySystemBarPadding(binding.getRoot());
        }

        // inflate tool bar (on the top)
        binding.toolbar.inflateMenu(R.menu.menu_student_toolbar);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                session.logout();
                returnToLogin();
                return true;
            }
            return false;
        });

        // Handling for each tabs of bottom menu
        binding.bottomNav.setOnItemSelectedListener(item -> {
            showTab(item.getItemId());
            return true;
        });

        // Whenever init the activity the value is null which we should show events tab (home)
        if (savedInstanceState == null) {
            // this also triggers the listener above
            binding.bottomNav.setSelectedItemId(R.id.nav_events);
        } else {
            // After rotation the fragment is restored
            // Showing title based on the tab
            binding.toolbar.setTitle(titleFor(binding.bottomNav.getSelectedItemId()));
        }

        /**
         *  getOnBackPressedDispatcher() is using for handling back button on the phone
         *  Back on any other tab goes to Events
         *  Back on Events leaves the app.
         * @see https://developer.android.com/guide/navigation/custom-back
         */
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.bottomNav.getSelectedItemId() != R.id.nav_events) {
                    binding.bottomNav.setSelectedItemId(R.id.nav_events);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    /**
     * Handling switching tabs
     *
     * @param itemId
     */
    private void showTab(int itemId) {
        int titleRes = titleFor(itemId);
        Fragment fragment;

        if (itemId == R.id.nav_events) {
            fragment = new AvailableEventsFragment();
        } else if (itemId == R.id.nav_history){
            fragment = new RegistrationHistoryFragment();
        } else if (itemId == R.id.nav_registrations){
            fragment = new MyRegistrationFragment();
        } else {
            // TODO: replace each placeholder when its screen is built (Considering switch: cases)
            fragment = StudentPlaceholderFragment.newInstance(getString(titleRes));
        }
        binding.toolbar.setTitle(titleRes);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.studentFragmentContainer, fragment)
                .commit();
    }

    /**
     * Get title name for Tool bar
     *
     * @param itemId
     * @return
     */
    private int titleFor(int itemId) {
        if (itemId == R.id.nav_registrations) return R.string.title_my_registrations;
        if (itemId == R.id.nav_history) return R.string.title_registration_history;
        if (itemId == R.id.nav_notifications) return R.string.title_notifications;
        return R.string.title_available_events;
    }

     private void returnToLogin() {
     Intent intent = new Intent(this, LoginActivity.class);
     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
     startActivity(intent);
     finish();
     }
}