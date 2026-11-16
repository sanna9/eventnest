package com.example.eventnest.ui.organizer;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventnest.data.database.AppDatabase;
import com.example.eventnest.data.entity.Event;
import com.example.eventnest.data.model.Role;
import com.example.eventnest.databinding.ActivityEventFormBinding;
import com.example.eventnest.util.AppExecutors;
import com.example.eventnest.util.DateUtils;
import com.example.eventnest.util.SessionManager;
import com.example.eventnest.util.UiUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import com.example.eventnest.R;

public class EventFormActivity extends AppCompatActivity {

    ActivityEventFormBinding binding;
    AppDatabase db;
    SessionManager session;
    int eventID;
    String selectedDate = "";
    String selectedTime = "";

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

        binding = ActivityEventFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            UiUtils.applySystemBarPadding(binding.getRoot());
        }

        db = AppDatabase.getInstance(this);
        eventID = getIntent().getIntExtra("event_id", -1);

        binding.buttonBack.setOnClickListener(v -> finish());
        binding.buttonSelectDate.setOnClickListener(v -> selectDate());
        binding.buttonSelectTime.setOnClickListener(v -> selectTime());
        binding.buttonSave.setOnClickListener(v -> saveEvent());

        if (savedInstanceState != null) {
            selectedDate = savedInstanceState.getString("date", "");
            selectedTime = savedInstanceState.getString("time", "");
            displayDateAndTime();
        }

        if (eventID != -1) {
            binding.textFormTitle.setText(R.string.title_edit_event);
            if (savedInstanceState == null) {
                loadEvent();
            }
        }
    }

    /** stores the date as yyyy-MM-dd */
    private void selectDate() {
        LocalDate date = selectedDate.isEmpty()
                ? LocalDate.now()
                : LocalDate.parse(selectedDate);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    selectedDate = DateUtils.formatDate(year, month, day);
                    displayDateAndTime();
                },
                date.getYear(),
                date.getMonthValue() - 1,
                date.getDayOfMonth()
        );

        dialog.show();
    }

    /** stores the time as HH:mm */
    private void selectTime() {
        LocalTime time = selectedTime.isEmpty()
                ? LocalTime.now()
                : LocalTime.parse(selectedTime);

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hour, minute) -> {
                    selectedTime = DateUtils.formatTime(hour, minute);
                    displayDateAndTime();
                },
                time.getHour(),
                time.getMinute(),
                false
        );

        dialog.show();
    }

    private void displayDateAndTime() {
        if (!selectedDate.isEmpty()) {
            binding.buttonSelectDate.setText(
                    DateUtils.displayDate(selectedDate)
            );
        }

        if (!selectedTime.isEmpty()) {
            binding.buttonSelectTime.setText(
                    DateUtils.displayTime(selectedTime)
            );
        }
    }

    /** fetch existing event, fills edit form
     * Only the event organizer can edit it */
    private void loadEvent() {
        binding.buttonSave.setEnabled(false);

        AppExecutors.run(() -> {
            try {
                Event event = db.eventDao().getEventByID(eventID);

                AppExecutors.runOnMain(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    if (event == null
                            || event.getOrganizerID() != session.getUserId()) {
                        showMessage(getString(R.string.error_event_access));
                        finish();
                        return;
                    }

                    binding.inputTitle.setText(event.getTitle());
                    binding.inputDescription.setText(event.getDescription());
                    binding.inputLocation.setText(event.getLocation());
                    binding.inputCapacity.setText(
                            String.valueOf(event.getCapacity())
                    );

                    selectedDate = event.getEventDate();
                    selectedTime = event.getEventTime();

                    displayDateAndTime();
                    binding.buttonSave.setEnabled(true);
                });

            } catch (Exception e) {
                showDatabaseError();
            }
        });
    }

    /** Checks the input, creates or updates the event in Room */
    private void saveEvent() {
        String title = binding.inputTitle.getText().toString().trim();
        String description =
                binding.inputDescription.getText().toString().trim();
        String location =
                binding.inputLocation.getText().toString().trim();
        String capacityText =
                binding.inputCapacity.getText().toString().trim();

        if (title.isEmpty()) {
            binding.inputTitle.setError(getString(R.string.error_event_title_required));
            return;
        }

        if (description.isEmpty()) {
            binding.inputDescription.setError(getString(R.string.error_event_description_required));
            return;
        }

        if (location.isEmpty()) {
            binding.inputLocation.setError(getString(R.string.error_event_location_required));
            return;
        }

        int capacity;

        try {
            capacity = Integer.parseInt(capacityText);

            if (capacity <= 0) {
                binding.inputCapacity.setError(getString(R.string.error_capacity_positive));
                return;
            }
        } catch (NumberFormatException e) {
            binding.inputCapacity.setError(getString(R.string.error_capacity_invalid));
            return;
        }

        if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
            showMessage(getString(R.string.error_event_datetime_required));
            return;
        }

        // copy the form values before starting background work
        Event event = new Event();
        event.setOrganizerID(session.getUserId());
        event.setTitle(title);
        event.setDescription(description);
        event.setLocation(location);
        event.setCapacity(capacity);
        event.setEventDate(selectedDate);
        event.setEventTime(selectedTime);

        binding.buttonSave.setEnabled(false);

        AppExecutors.run(() -> {
            try {
                if (eventID == -1) {
                    db.eventDao().addEvent(event);

                } else {
                    // recheck ownership before updating the record
                    Event existing = db.eventDao().getEventByID(eventID);

                    if (existing == null
                            || existing.getOrganizerID()
                            != event.getOrganizerID()) {
                        AppExecutors.runOnMain(() -> {
                            if (isFinishing() || isDestroyed()) {
                                return;
                            }

                            showMessage(getString(R.string.error_event_access));
                            finish();
                        });
                        return;
                    }

                    event.setEventID(eventID);
                    db.eventDao().updateEvent(event);
                }

                AppExecutors.runOnMain(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    showMessage(getString(R.string.toast_event_saved));
                    finish();
                });

            } catch (Exception e) {
                showDatabaseError();
            }
        });
    }

    /** returns database error feedback to the main thread */
    private void showDatabaseError() {
        AppExecutors.runOnMain(() -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }

            binding.buttonSave.setEnabled(true);
            showMessage(getString(R.string.error_database_operation));
        });
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /** Keeps selected date and time when app recreates the screen
     * saves the EditText values automatically */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("date", selectedDate);
        outState.putString("time", selectedTime);
        super.onSaveInstanceState(outState);
    }
}