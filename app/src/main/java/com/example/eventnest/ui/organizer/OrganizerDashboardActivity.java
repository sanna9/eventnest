package com.example.eventnest.ui.organizer;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventnest.R;
import com.example.eventnest.data.model.Role;
import com.example.eventnest.databinding.ActivityOrganizerDashboardBinding;
import com.example.eventnest.ui.auth.LoginActivity;
import com.example.eventnest.util.SessionManager;
import com.example.eventnest.util.UiUtils;

public class OrganizerDashboardActivity extends AppCompatActivity {

    ActivityOrganizerDashboardBinding binding;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(this);

        if (!session.isLoggedIn()
                || session.getRole() != Role.ORGANIZER) {
            returnToLogin();
            return;
        }

        EdgeToEdge.enable(this);

        binding = ActivityOrganizerDashboardBinding.inflate(
                getLayoutInflater()
        );
        setContentView(binding.getRoot());

        // resuing the padding helper.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            UiUtils.applySystemBarPadding(binding.getRoot());
        }

        binding.cardManageEvents.setOnClickListener(v ->
                openManageEvents()
        );

        // screens will be connected in upcoming tasks
        binding.cardRegisteredStudents.setOnClickListener(v ->
                showMessage("Registered Students")
        );

        binding.cardMonthlyReport.setOnClickListener(v ->
                showMessage("Monthly Report")
        );

        binding.cardSendNotification.setOnClickListener(v ->
                showMessage("Send Notification")
        );

        binding.buttonLogout.setOnClickListener(v -> {
            session.logout();
            returnToLogin();
        });

        // Update the visible screen when a Fragment opens or Back is pressed
        getSupportFragmentManager().addOnBackStackChangedListener(
                () -> updateDashboardVisibility()
        );

        // Also handles a Fragment restored after screen rotation
        updateDashboardVisibility();
    }

    /* Opens Manage Events and remembers the dashboard in navigation history */
    private void openManageEvents() {

        // won't allow repeated taps from opening duplicate screens
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            return;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.organizerFragmentContainer,
                        new EventListFragment()
                )
                .addToBackStack("manage_events")
                .commit();
    }

    /*Shows either the dashboard controls or the Fragment screen
     showing the Fragment from the back stack restores the dashboard */
    private void updateDashboardVisibility() {

        boolean fragmentOpen =
                getSupportFragmentManager().getBackStackEntryCount() > 0;

        int dashboardVisibility =
                fragmentOpen ? View.GONE : View.VISIBLE;

        binding.textDashboardTitle.setVisibility(dashboardVisibility);
        binding.dashboardCards.setVisibility(dashboardVisibility);
        binding.buttonLogout.setVisibility(dashboardVisibility);

        binding.organizerFragmentContainer.setVisibility(
                fragmentOpen ? View.VISIBLE : View.GONE
        );
    }

    /** Displays temporary feedback for unfinished sections */
    private void showMessage(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }


    /** Opens login and clears previous screens from navigation history  **/
    private void returnToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}