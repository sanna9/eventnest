package com.example.eventnest.ui.shared;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventnest.R;
import com.example.eventnest.data.dao.RegistrationDao;
import com.example.eventnest.data.database.AppDatabase;
import com.example.eventnest.data.entity.Event;
import com.example.eventnest.data.entity.Registration;
import com.example.eventnest.data.model.Role;
import com.example.eventnest.databinding.ActivityEventDetailsBinding;
import com.example.eventnest.ui.organizer.EventFormActivity;
import com.example.eventnest.util.AppExecutors;
import com.example.eventnest.util.DateUtils;
import com.example.eventnest.util.SessionManager;
import com.example.eventnest.util.UiUtils;

/**
 * Event details, shared by both roles.
 * Organizer sees the registered count and edit and delete buttons (own event only)
 * Student sees seats left and a register button
 */
public class EventDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "event_id";
    private static final String STATUS_ACTIVE = "Active";

    // State of register button, what it should show
    private enum RegisterState {CAN_REGISTER, ALREADY_REGISTERED, FULL, PAST}

    // Register button result, handling when anomaly happens
    private enum RegisterResult {OK, ALREADY_REGISTERED, FULL, PAST, NOT_FOUND}

    ActivityEventDetailsBinding binding;
    AppDatabase db;
    SessionManager session;

    private Role role;
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
        role = session.getRole();
        if (!session.isLoggedIn() || role == null) {
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

        // Organizer's button
        boolean isOrganizer = (role == Role.ORGANIZER);
        binding.layoutOrganizerActions.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
        binding.layoutStudentActions.setVisibility(isOrganizer ? View.GONE : View.VISIBLE);
        binding.textRegisteredLabel
                .setText(isOrganizer ? R.string.label_registered : R.string.label_seats_left);

        setActionsEnabled(false);

        binding.buttonBack.setOnClickListener(v -> finish());
        binding.buttonEdit.setOnClickListener(v -> openEditForm());
        binding.buttonDelete.setOnClickListener(v -> confirmDelete());
        // TODO: CONFIRM REGISTER
        binding.buttonRegister.setOnClickListener(v -> confirmRegister());
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

                // Check if user is organizer, if so check whether is the same organizer of the event
                boolean isAllowed = (role == Role.ORGANIZER
                        && event != null && event.getOrganizerID() != session.getUserId());
                if (event == null || isAllowed) {
                    closeUnavailableEvent();
                    return;
                }

                int registeredCount = db.registrationDao()
                        .getRegisteredStudents(eventID)
                        .size();

                RegisterState state = null;
                if (role == Role.STUDENT) {
                    state = findingRegisterState(event, registeredCount);
                }

                RegisterState finalState = state;
                AppExecutors.runOnMain(() -> showEvent(event, registeredCount, finalState));

            } catch (Exception e) {
                showDatabaseError();
            }
        });
    }

    /**
     * Runs on the main thread
     *
     * @param event           event from database
     * @param registeredCount the number of registered (get from database which is in DAO)
     * @param state           the state of the register button
     */
    private void showEvent(Event event, int registeredCount, RegisterState state) {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        binding.textEventTitle.setText(event.getTitle());
        binding.textEventDate.setText(DateUtils.displayDate(event.getEventDate()));
        binding.textEventTime.setText(DateUtils.displayTime(event.getEventTime()));
        binding.textEventLocation.setText(event.getLocation());
        binding.textEventDescription.setText(event.getDescription());

        if (role == Role.ORGANIZER) {
            binding.textRegisteredCount.setText(getString(
                    R.string.event_registration_count, registeredCount, event.getCapacity()));
            setActionsEnabled(true);
        } else {
            int seatsLeft = Math.max(event.getCapacity() - registeredCount, 0);
            binding.textRegisteredCount.setText(getString(
                    R.string.event_seats_left, seatsLeft, event.getCapacity()));
            showRegisterButton(state);
        }
    }

    /**
     * find register state, in case past due or unavailable
     * @param event current event
     * @param registeredCount the number of the registration
     * @return register state (CAN_REGISTER, ALREADY_REGISTERED, FULL, PAST)
     */
    private RegisterState findingRegisterState(Event event, int registeredCount) {
        if (event.getEventDate().compareTo(DateUtils.today()) < 0) {
            return RegisterState.PAST;
        }

        Registration existing = db.registrationDao().getRegistration(session.getUserId(), eventID);
        if (existing != null && STATUS_ACTIVE.equals(existing.getStatus())) {
            return RegisterState.ALREADY_REGISTERED;
        }

        if (registeredCount >= event.getCapacity()) {
            return RegisterState.FULL;
        }
        return RegisterState.CAN_REGISTER;
    }

    /**
     * Show register button based on state
     * @param state register state
     */
    private void showRegisterButton(RegisterState state) {
        switch (state) {
            // Available
            case CAN_REGISTER:
                binding.buttonRegister.setText(R.string.button_register);
                binding.buttonRegister.setEnabled(true);
                break;
            case ALREADY_REGISTERED:
                binding.buttonRegister.setText(R.string.button_already_registered);
                binding.buttonRegister.setEnabled(false);
                break;
            case FULL:
                binding.buttonRegister.setText(R.string.button_event_full);
                binding.buttonRegister.setEnabled(false);
                break;
            // PAST
            default:
                binding.buttonRegister.setText(R.string.button_event_past);
                binding.buttonRegister.setEnabled(false);
        }
    }

    /**
     * Confirm dialog in case user press wrong button or miss click
     */
    private void confirmRegister() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_register_title)
                .setMessage(R.string.dialog_register_message)
                .setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.button_register,
                        (dialog, which) -> registerForEvent())
                .show();
    }

    private void registerForEvent() {
        // stops double taps
        setActionsEnabled(false);
        int studentId = session.getUserId();

        AppExecutors.run(() -> {
            try {
                // All checks and the insert happen in one transaction,
                // so two registrations cannot both take the last seat.
                RegisterResult result = db.runInTransaction(() -> registerStudent(studentId));
                AppExecutors.runOnMain(() -> onRegisterResult(result));
            } catch (Exception e) {
                showDatabaseError();
            }
        });
    }

    /**
     * Get result or outcome of the registration
     * run on main thread
     * @param studentId from session
     * @return output of the registration button
     */
    private RegisterResult registerStudent(int studentId) {
        Event event = db.eventDao().getEventByID(eventID);
        if (event == null) {
            return RegisterResult.NOT_FOUND;
        }
        // Past due
        if (event.getEventDate().compareTo(DateUtils.today()) < 0) {
            return RegisterResult.PAST;
        }

        RegistrationDao registrationDao = db.registrationDao();
        Registration existing = registrationDao.getRegistration(studentId, eventID);

        if (existing != null && STATUS_ACTIVE.equals(existing.getStatus())) {
            return RegisterResult.ALREADY_REGISTERED;
        }
        if (registrationDao.countActiveRegistrations(eventID) >= event.getCapacity()) {
            return RegisterResult.FULL;
        }

        String today = DateUtils.today();
        if (existing == null) {
            // add a new registration if new
            Registration registration = new Registration();
            registration.setStudentID(studentId);
            registration.setEventID(eventID);
            registration.setRegistrationDate(today);
            registration.setStatus(STATUS_ACTIVE);
            registrationDao.addRegistration(registration);
        } else {
            // If the user cancelled once, it will reuse that registration
            registrationDao.reactivateRegistration(studentId, eventID, today);
        }
        return RegisterResult.OK;
    }

    /**
     * get the outcome of the register button
     * @param result
     */
    private void onRegisterResult(RegisterResult result) {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        switch (result) {
            case OK:
                showMessage(R.string.toast_registered);
                break;
            case ALREADY_REGISTERED:
                showMessage(R.string.error_already_registered);
                break;
            case FULL:
                showMessage(R.string.error_event_full);
                break;
            case PAST:
                showMessage(R.string.error_event_passed);
                break;
            // NOT_FOUND
            default:
                showMessage(R.string.error_event_access);
                finish();
                return;
        }

        // Reload so the seat count and the button show the new state
        loadEvent();
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