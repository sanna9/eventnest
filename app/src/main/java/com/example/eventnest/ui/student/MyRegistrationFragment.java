package com.example.eventnest.ui.student;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.eventnest.R;
import com.example.eventnest.data.database.AppDatabase;
import com.example.eventnest.data.entity.Event;
import com.example.eventnest.databinding.FragmentRegistrationListBinding;
import com.example.eventnest.ui.adapters.RegistrationAdapter;
import com.example.eventnest.ui.shared.EventDetailsActivity;
import com.example.eventnest.util.AppExecutors;
import com.example.eventnest.util.DateUtils;
import com.example.eventnest.util.SessionManager;

import java.util.HashSet;
import java.util.List;

public class MyRegistrationFragment extends Fragment {
    private FragmentRegistrationListBinding binding;
    private AppDatabase db;
    private SessionManager session;
    private RegistrationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegistrationListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = AppDatabase.getInstance(requireContext());
        session = new SessionManager(requireContext());

        binding.textEmpty.setText(R.string.empty_registrations);
        // This fragment is for student's event registration which is located on student's side bottom tab
        adapter = new RegistrationAdapter(RegistrationAdapter.Mode.CURRENT,
                event -> openDetails(event),
                event -> confirmCancel(event));
        binding.recyclerRegistrations.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerRegistrations.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRegistrations();
    }

    private void loadRegistrations() {
        FragmentRegistrationListBinding requestBinding = binding;
        int studentId = session.getUserId();
        String today = DateUtils.today();

        // Background thread for reading database
        AppExecutors.run(() -> {
            try {
                List<Event> events = db.registrationDao().getCurrentRegisteredEvents(studentId, today);
                // Main thread for updating the screen
                AppExecutors.runOnMain(() -> showEvents(requestBinding, events));
            } catch (Exception e) {
                AppExecutors.runOnMain(() -> showError(requestBinding, R.string.error_load_events));
            }
        });
    }

    private void showEvents(FragmentRegistrationListBinding requestBinding, List<Event> events) {
        if (binding == null || binding != requestBinding) return;

        adapter.setItems(events, new HashSet<>());
        binding.textEmpty.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showError(FragmentRegistrationListBinding requestBinding, int messageRes) {
        if (binding == null || binding != requestBinding) return;
        Toast.makeText(requireContext(), messageRes, Toast.LENGTH_LONG).show();
    }

    private void openDetails(Event event) {
        Intent intent = new Intent(requireContext(), EventDetailsActivity.class);
        intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, event.getEventID());
        startActivity(intent);
    }

    /**
     * Displays a confirmation dialog before canceling registration for the given event.
     *
     * @param event the event whose registration may be canceled
     * @see <a href="https://developer.android.com/reference/androidx/appcompat/app/AlertDialog.Builder#setPositiveButton(int,android.content.DialogInterface.OnClickListener)">AndroidX AlertDialog.Builder#setPositiveButton</a>
     */
    private void confirmCancel(Event event) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_cancel_registration_title)
                .setMessage(getString(R.string.dialog_cancel_registration_message, event.getTitle()))
                .setNegativeButton(R.string.button_keep_registration, null)
                .setPositiveButton(R.string.button_cancel_registration,
                        (dialog, which) -> cancelRegistration(event))
                .show();
    }

    private void cancelRegistration(Event event) {
        FragmentRegistrationListBinding requestBinding = binding;
        int studentId = session.getUserId();

        AppExecutors.run(() -> {
            try {
                int rows = db.registrationDao().cancelRegistration(studentId, event.getEventID());
                AppExecutors.runOnMain(() -> {
                    // This condition is for checking and guarding
                    // in case fragment destroyed by use's actions
                    if (binding == null || binding != requestBinding) return;
                    Toast.makeText(requireContext(),
                            rows == 1 ? R.string.toast_registration_cancelled
                                    : R.string.error_database_operation,
                            Toast.LENGTH_SHORT).show();
                    // Refresh list
                    loadRegistrations();
                });
            } catch (Exception e) {
                AppExecutors.runOnMain(() -> {
                    showError(requestBinding, R.string.error_database_operation);
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        binding.recyclerRegistrations.setAdapter(null);
        binding = null;
        adapter = null;
        super.onDestroyView();
    }
}
