package com.example.eventnest.ui.organizer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.eventnest.R;
import com.example.eventnest.data.database.AppDatabase;
import com.example.eventnest.data.entity.Event;
import com.example.eventnest.data.entity.User;
import com.example.eventnest.databinding.FragmentRegisteredStudentsBinding;
import com.example.eventnest.ui.adapters.RegisteredStudentAdapter;
import com.example.eventnest.util.AppExecutors;
import com.example.eventnest.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class RegisteredStudentsFragment extends Fragment {

    FragmentRegisteredStudentsBinding binding;
    AppDatabase db;
    SessionManager session;
    RegisteredStudentAdapter adapter;

    List<Event> eventList = new ArrayList<>();

    // Helps ignore old results when the selected event changes.
    int studentRequest = 0;

    /**
     * Creates the screen using ViewBinding.
     */
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentRegisteredStudentsBinding.inflate(
                inflater, container, false
        );

        return binding.getRoot();
    }

    /**
     * Connects the student list, Back button and event Spinner.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = AppDatabase.getInstance(requireContext());
        session = new SessionManager(requireContext());

        adapter = new RegisteredStudentAdapter();

        binding.recyclerStudents.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        binding.recyclerStudents.setAdapter(adapter);

        binding.buttonBack.setOnClickListener(v ->
                requireActivity()
                        .getOnBackPressedDispatcher()
                        .onBackPressed()
        );

        binding.spinnerEvents.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View selectedView,
                            int position,
                            long id
                    ) {
                        Event event = eventList.get(position);
                        loadStudents(event.getEventID());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // No event selected.
                    }
                }
        );

        loadEvents();
    }

    /**
     * Loads the signed-in organizer's events into the Spinner.
     */
    private void loadEvents() {
        int organizerID = session.getUserId();

        FragmentRegisteredStudentsBinding currentBinding = binding;
        binding.spinnerEvents.setEnabled(false);

        AppExecutors.run(() -> {
            try {
                List<Event> events = db.eventDao()
                        .getEventsByOrganizer(organizerID);

                AppExecutors.runOnMain(() -> {
                    if (binding == null || binding != currentBinding) {
                        return;
                    }

                    eventList = events;

                    if (events.isEmpty()) {
                        binding.textEmpty.setText(
                                R.string.message_no_events
                        );
                        binding.textEmpty.setVisibility(View.VISIBLE);
                        binding.textTotalRegistered.setText(
                                getString(R.string.total_registered_count, 0)
                        );
                        return;
                    }

                    List<String> titles = new ArrayList<>();

                    for (Event event : events) {
                        titles.add(event.getTitle());
                    }

                    ArrayAdapter<String> eventAdapter =
                            new ArrayAdapter<>(
                                    requireContext(),
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
                    if (binding == null || binding != currentBinding) {
                        return;
                    }

                    binding.textEmpty.setText(
                            R.string.error_organizer_events_load
                    );
                    binding.textEmpty.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    /**
     * Loads active registered students for the selected event.
     * Updates views only if this is still the latest selection.
     */
    private void loadStudents(int eventID) {
        int request = ++studentRequest;

        // Clear the previous event's students while loading.
        adapter.setStudents(new ArrayList<>());
        binding.textEmpty.setVisibility(View.GONE);
        binding.textTotalRegistered.setText("");

        AppExecutors.run(() -> {
            try {
                List<User> students = db.registrationDao()
                        .getRegisteredStudents(eventID);

                AppExecutors.runOnMain(() -> {
                    if (binding == null || request != studentRequest) {
                        return;
                    }

                    adapter.setStudents(students);

                    binding.textEmpty.setText(
                            R.string.message_no_registered_students
                    );
                    binding.textEmpty.setVisibility(
                            students.isEmpty() ? View.VISIBLE : View.GONE
                    );

                    binding.textTotalRegistered.setText(
                            getString(
                                    R.string.total_registered_count,
                                    students.size()
                            )
                    );
                });

            } catch (Exception e) {
                AppExecutors.runOnMain(() -> {
                    if (binding == null || request != studentRequest) {
                        return;
                    }

                    Toast.makeText(
                            requireContext(),
                            R.string.error_registered_students_load,
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    /**
     * Releases the screen's views and invalidates pending student results.
     */
    @Override
    public void onDestroyView() {
        studentRequest++;

        binding.recyclerStudents.setAdapter(null);
        binding.spinnerEvents.setOnItemSelectedListener(null);
        binding = null;
        adapter = null;

        super.onDestroyView();
    }
}