package com.example.eventnest.ui.organizer;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventnest.R;
import com.example.eventnest.data.database.AppDatabase;
import com.example.eventnest.data.entity.Event;
import com.example.eventnest.data.model.Role;
import com.example.eventnest.databinding.ActivityEventDetailsBinding;
import com.example.eventnest.util.AppExecutors;
import com.example.eventnest.util.DateUtils;
import com.example.eventnest.util.SessionManager;
import com.example.eventnest.util.UiUtils;

public class EventDetailsActivity extends AppCompatActivity {

    ActivityEventDetailsBinding binding;
    AppDatabase db;
    SessionManager session;

    int eventID;
    boolean deleting = false;

    /**
     * Checks organizer access and connects the screen controls.
     * Student behaviour will be integrated with the student-side owner.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(this);

        if (!session.isLoggedIn()
                || session.getRole() != Role.ORGANIZER) {
            finish();
            return;
        }

        EdgeToEdge.enable(this);

        binding = ActivityEventDetailsBinding.inflate(
                getLayoutInflater()
        );
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            UiUtils.applySystemBarPadding(binding.getRoot());
        }

        db = AppDatabase.getInstance(this);
        eventID = getIntent().getIntExtra("event_id", -1);

        if (eventID == -1) {
            showMessage(R.string.error_event_access);
            finish();
            return;
        }

        setActionsEnabled(false);

        binding.buttonBack.setOnClickListener(v -> finish());
        binding.buttonEdit.setOnClickListener(v -> openEditForm());
        binding.buttonDelete.setOnClickListener(v -> confirmDelete());
    }

    /**
     * Reloads details when opening the screen or returning from editing.
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (binding != null && !isFinishing() && !deleting) {
            loadEvent();
        }
    }

    /**
     * Fetches the event and active registration count in the background.
     * Displays the results on the main thread.
     */
    private void loadEvent() {
        setActionsEnabled(false);

        AppExecutors.run(() -> {
            try {
                Event event = db.eventDao().getEventByID(eventID);

                if (event == null
                        || event.getOrganizerID() != session.getUserId()) {
                    closeUnavailableEvent();
                    return;
                }

                int registeredCount = db.registrationDao()
                        .getRegisteredStudents(eventID)
                        .size();

                AppExecutors.runOnMain(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    binding.textEventTitle.setText(event.getTitle());

                    binding.textEventDate.setText(
                            DateUtils.displayDate(event.getEventDate())
                    );

                    binding.textEventTime.setText(
                            DateUtils.displayTime(event.getEventTime())
                    );

                    binding.textEventLocation.setText(
                            event.getLocation()
                    );

                    binding.textRegisteredCount.setText(
                            getString(
                                    R.string.event_registration_count,
                                    registeredCount,
                                    event.getCapacity()
                            )
                    );

                    binding.textEventDescription.setText(
                            event.getDescription()
                    );

                    setActionsEnabled(true);
                });

            } catch (Exception e) {
                showDatabaseError();
            }
        });
    }

    /**
     * Passes the event ID to the existing create/edit form.
     */
    private void openEditForm() {
        Intent intent = new Intent(this, EventFormActivity.class);
        intent.putExtra("event_id", eventID);
        startActivity(intent);
    }

    /**
     * Explains the cascade deletion before asking for confirmation.
     */
    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_event_title)
                .setMessage(R.string.dialog_delete_event_message)
                .setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(
                        R.string.button_delete,
                        (dialog, which) -> deleteEvent()
                )
                .show();
    }

    /**
     * Rechecks ownership and deletes the event in the background.
     * Room's foreign keys remove linked registrations and notifications.
     */
    private void deleteEvent() {
        deleting = true;
        setActionsEnabled(false);

        AppExecutors.run(() -> {
            try {
                Event event = db.eventDao().getEventByID(eventID);

                if (event == null
                        || event.getOrganizerID() != session.getUserId()) {
                    closeUnavailableEvent();
                    return;
                }

                int deletedRows = db.eventDao().deleteEvent(eventID);

                AppExecutors.runOnMain(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    showMessage(
                            deletedRows == 1
                                    ? R.string.toast_event_deleted
                                    : R.string.error_event_access
                    );

                    finish();
                });

            } catch (Exception e) {
                showDatabaseError();
            }
        });
    }

    /**
     * Closes the screen if the event is missing or belongs to someone else.
     */
    private void closeUnavailableEvent() {
        AppExecutors.runOnMain(() -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }

            showMessage(R.string.error_event_access);
            finish();
        });
    }

    /**
     * Displays a database error without enabling unverified actions.
     * The user can go Back and reopen the screen to retry.
     */
    private void showDatabaseError() {
        AppExecutors.runOnMain(() -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }

            deleting = false;
            setActionsEnabled(false);
            showMessage(R.string.error_database_operation);
        });
    }

    private void setActionsEnabled(boolean enabled) {
        binding.buttonEdit.setEnabled(enabled);
        binding.buttonDelete.setEnabled(enabled);
    }

    private void showMessage(int stringResource) {
        Toast.makeText(
                this,
                stringResource,
                Toast.LENGTH_SHORT
        ).show();
    }
}