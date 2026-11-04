package com.example.eventnest.ui.organizer;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

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

        // Only a signed-in organizer can access this screen.
        if (!session.isLoggedIn()
                || session.getRole() != Role.ORGANIZER) {

            returnToLogin();
            return;
        }

        EdgeToEdge.enable(this);

        // display dashboard using ViewBinding
        binding = ActivityOrganizerDashboardBinding.inflate(
                getLayoutInflater()
        );
        setContentView(binding.getRoot());
        // Apply system-bar padding using the existing helper.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            UiUtils.applySystemBarPadding(binding.getRoot());
        }

        // Temporary messages until the destination screens are built.
        binding.cardManageEvents.setOnClickListener(v ->
                showMessage("Manage Events")
        );

        binding.cardRegisteredStudents.setOnClickListener(v ->
                showMessage("Registered Students")
        );

        binding.cardMonthlyReport.setOnClickListener(v ->
                showMessage("Monthly Report")
        );

        binding.cardSendNotification.setOnClickListener(v ->
                showMessage("Send Notification")
        );

        // Clear the saved session and return to login
        binding.buttonLogout.setOnClickListener(v -> {
            session.logout();
            returnToLogin();
        });
    }

    // Temporarily confirms which card was tapped.
    private void showMessage(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    // Clear the navigation history so Back cannot reopen the dashboard.
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