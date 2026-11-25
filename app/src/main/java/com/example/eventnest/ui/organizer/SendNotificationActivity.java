package com.example.eventnest.ui.organizer;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventnest.R;
import com.example.eventnest.data.database.AppDatabase;
import com.example.eventnest.data.entity.Event;
import com.example.eventnest.data.entity.Notification;
import com.example.eventnest.data.entity.User;
import com.example.eventnest.data.model.Role;
import com.example.eventnest.databinding.ActivitySendNotificationBinding;
import com.example.eventnest.util.AppExecutors;
import com.example.eventnest.util.DateUtils;
import com.example.eventnest.util.SessionManager;
import com.example.eventnest.util.UiUtils;

import java.util.ArrayList;
import java.util.List;

public class SendNotificationActivity extends AppCompatActivity {

    ActivitySendNotificationBinding binding;
    AppDatabase db;
    SessionManager session;

    List<Event> eventList = new ArrayList<>();

    int selectedEventID = -1;
    int recipientCount = 0;
    int recipientRequest = 0;

    boolean recipientsReady = false;
    boolean sending = false;

    /**
     * Checks organizer access and connects the screen using ViewBinding.
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

        binding = ActivitySendNotificationBinding.inflate(
                getLayoutInflater()
        );
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            UiUtils.applySystemBarPadding(binding.getRoot());
        }

        db = AppDatabase.getInstance(this);

        binding.buttonBack.setOnClickListener(v -> finish());
        binding.buttonSend.setOnClickListener(v -> sendNotification());

        binding.spinnerEvents.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {
                        if (position >= 0 && position < eventList.size()) {
                            selectedEventID =
                                    eventList.get(position).getEventID();

                            loadRecipients();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        selectedEventID = -1;
                        recipientsReady = false;
                        binding.buttonSend.setEnabled(false);
                    }
                }
        );

        loadEvents();
    }

    /**
     * Loads only events belonging to the signed-in organizer.
     */
    private void loadEvents() {
        int organizerID = session.getUserId();

        AppExecutors.run(() -> {
            try {
                List<Event> events = db.eventDao()
                        .getEventsByOrganizer(organizerID);

                AppExecutors.runOnMain(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    eventList = events;

                    if (events.isEmpty()) {
                        binding.textRecipients.setText(
                                getString(
                                        R.string.notification_recipients_count,
                                        0
                                )
                        );
                        showError(R.string.message_no_events);
                        return;
                    }

                    List<String> titles = new ArrayList<>();

                    for (Event event : events) {
                        titles.add(event.getTitle());
                    }

                    ArrayAdapter<String> eventAdapter =
                            new ArrayAdapter<>(
                                    this,
                                    android.R.layout.simple_spinner_item,
                                    titles
                            );

                    eventAdapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item
                    );

                    binding.spinnerEvents.setAdapter(eventAdapter);
                    binding.spinnerEvents.setEnabled(true);
                });

            } catch (Exception e) {
                AppExecutors.runOnMain(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    binding.textRecipients.setText("");
                    showError(R.string.error_organizer_events_load);
                });
            }
        });
    }

    /**
     * Displays the active recipient count for the selected event.
     * Ignores results from an older selection.
     */
    private void loadRecipients() {
        int eventID = selectedEventID;
        int request = ++recipientRequest;

        recipientsReady = false;
        recipientCount = 0;

        binding.buttonSend.setEnabled(false);
        binding.textError.setVisibility(View.GONE);
        binding.textRecipients.setText(
                R.string.notification_recipients_loading
        );

        AppExecutors.run(() -> {
            try {
                int count = db.registrationDao()
                        .countActiveRegistrations(eventID);

                AppExecutors.runOnMain(() -> {
                    if (isFinishing()
                            || isDestroyed()
                            || request != recipientRequest) {
                        return;
                    }

                    recipientCount = count;
                    recipientsReady = true;

                    binding.textRecipients.setText(
                            getString(
                                    R.string.notification_recipients_count,
                                    count
                            )
                    );

                    binding.buttonSend.setEnabled(count > 0 && !sending);

                    if (count == 0) {
                        showError(R.string.error_notification_no_recipients);
                    }
                });

            } catch (Exception e) {
                AppExecutors.runOnMain(() -> {
                    if (isFinishing()
                            || isDestroyed()
                            || request != recipientRequest) {
                        return;
                    }

                    binding.textRecipients.setText("");
                    showError(R.string.error_notification_recipients_load);
                });
            }
        });
    }

    /**
     * Validates the form and saves one notification per active student.
     * The transaction saves all messages together or rolls everything back.
     */
    private void sendNotification() {
        if (sending || !recipientsReady) {
            return;
        }

        binding.textError.setVisibility(View.GONE);

        if (selectedEventID == -1) {
            showError(R.string.error_notification_event_required);
            return;
        }

        String message = binding.inputMessage.getText()
                .toString().trim();

        if (message.isEmpty()) {
            binding.inputMessage.setError(
                    getString(R.string.error_notification_message_required)
            );
            binding.inputMessage.requestFocus();
            return;
        }

        if (recipientCount == 0) {
            showError(R.string.error_notification_no_recipients);
            return;
        }

        int eventID = selectedEventID;
        int organizerID = session.getUserId();

        sending = true;
        setSendingState(true);

        AppExecutors.run(() -> {
            try {
                int sentCount = db.runInTransaction(() -> {
                    // Recheck ownership before writing messages.
                    Event event = db.eventDao().getEventByID(eventID);

                    if (event == null
                            || event.getOrganizerID() != organizerID) {
                        return -1;
                    }

                    // Read recipients again because registrations may change.
                    List<User> students = db.registrationDao()
                            .getRegisteredStudents(eventID);

                    String sentDate = DateUtils.today();

                    for (User student : students) {
                        Notification notification = new Notification();

                        notification.setEventID(eventID);
                        notification.setStudentID(student.getUserID());
                        notification.setMessage(message);
                        notification.setSentDate(sentDate);

                        db.notificationDao().addNotification(notification);
                    }

                    return students.size();
                });

                AppExecutors.runOnMain(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    sending = false;
                    setSendingState(false);

                    if (sentCount == -1) {
                        showError(R.string.error_event_access);
                        binding.buttonSend.setEnabled(false);
                        return;
                    }

                    if (sentCount == 0) {
                        showError(R.string.error_notification_no_recipients);

                        recipientCount = 0;
                        binding.textRecipients.setText(
                                getString(
                                        R.string.notification_recipients_count,
                                        0
                                )
                        );
                        binding.buttonSend.setEnabled(false);
                        return;
                    }

                    Toast.makeText(
                            this,
                            getString(
                                    R.string.toast_notification_sent,
                                    sentCount
                            ),
                            Toast.LENGTH_LONG
                    ).show();

                    // Close after success to avoid sending the message again.
                    finish();
                });

            } catch (Exception e) {
                AppExecutors.runOnMain(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    sending = false;
                    setSendingState(false);
                    showError(R.string.error_notification_send);
                });
            }
        });
    }

    /**
     * Prevents repeated taps and form changes while sending.
     */
    private void setSendingState(boolean busy) {
        binding.spinnerEvents.setEnabled(!busy && !eventList.isEmpty());
        binding.inputMessage.setEnabled(!busy);

        binding.buttonSend.setEnabled(
                !busy && recipientsReady && recipientCount > 0
        );

        binding.buttonSend.setText(
                busy
                        ? R.string.notification_sending
                        : R.string.button_send_notification
        );
    }

    /**
     * Displays an error using an existing string resource.
     */
    private void showError(int messageResource) {
        binding.textError.setText(messageResource);
        binding.textError.setVisibility(View.VISIBLE);
    }
}