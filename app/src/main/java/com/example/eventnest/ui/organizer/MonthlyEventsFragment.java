package com.example.eventnest.ui.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.eventnest.R;
import com.example.eventnest.data.database.AppDatabase;
import com.example.eventnest.data.entity.Event;
import com.example.eventnest.databinding.FragmentMonthlyEventsBinding;
import com.example.eventnest.ui.adapters.MonthlyEventAdapter;
import com.example.eventnest.ui.shared.EventDetailsActivity;
import com.example.eventnest.util.AppExecutors;
import com.example.eventnest.util.SessionManager;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

public class MonthlyEventsFragment extends Fragment {

    FragmentMonthlyEventsBinding binding;
    AppDatabase db;
    SessionManager session;
    MonthlyEventAdapter adapter;

    boolean selectorsReady = false;
    int eventRequest = 0;

    /**
     * Creates the screen using ViewBinding.
     */
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentMonthlyEventsBinding.inflate(
                inflater, container, false
        );

        return binding.getRoot();
    }

    /**
     * Connects navigation, the event list and selectors.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = AppDatabase.getInstance(requireContext());
        session = new SessionManager(requireContext());

        adapter = new MonthlyEventAdapter(event -> {
            Intent intent = new Intent(
                    requireContext(),
                    EventDetailsActivity.class
            );

            intent.putExtra(
                    EventDetailsActivity.EXTRA_EVENT_ID,
                    event.getEventID()
            );

            startActivity(intent);
        });

        binding.recyclerEvents.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        binding.recyclerEvents.setAdapter(adapter);

        binding.buttonBack.setOnClickListener(v ->
                requireActivity()
                        .getOnBackPressedDispatcher()
                        .onBackPressed()
        );

        setupSelectors();
    }

    /**
     * Fills the month and year selectors.
     * Includes All months, the current year and years containing events.
     */
    private void setupSelectors() {
        FragmentMonthlyEventsBinding currentBinding = binding;
        int organizerID = session.getUserId();

        // Read the string while the Fragment is attached.
        String allMonthsLabel = getString(R.string.option_all_months);

        binding.spinnerMonth.setEnabled(false);
        binding.spinnerYear.setEnabled(false);

        AppExecutors.run(() -> {
            try {
                List<Event> events = db.eventDao()
                        .getEventsByOrganizer(organizerID);

                LocalDate today = LocalDate.now();

                // TreeSet keeps years sorted without duplicates.
                TreeSet<Integer> eventYears = new TreeSet<>();
                eventYears.add(today.getYear());

                for (Event event : events) {
                    eventYears.add(
                            LocalDate.parse(event.getEventDate()).getYear()
                    );
                }

                List<Integer> years = new ArrayList<>(eventYears);
                List<String> months = new ArrayList<>();

                // Position 0 represents all months in the selected year.
                months.add(allMonthsLabel);

                for (int month = 1; month <= 12; month++) {
                    months.add(
                            Month.of(month).getDisplayName(
                                    TextStyle.FULL,
                                    Locale.getDefault()
                            )
                    );
                }

                AppExecutors.runOnMain(() -> {
                    if (binding == null || binding != currentBinding) {
                        return;
                    }

                    ArrayAdapter<String> monthAdapter =
                            new ArrayAdapter<>(
                                    requireContext(),
                                    android.R.layout.simple_spinner_item,
                                    months
                            );

                    monthAdapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item
                    );

                    ArrayAdapter<Integer> yearAdapter =
                            new ArrayAdapter<>(
                                    requireContext(),
                                    android.R.layout.simple_spinner_item,
                                    years
                            );

                    yearAdapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item
                    );

                    binding.spinnerMonth.setAdapter(monthAdapter);
                    binding.spinnerYear.setAdapter(yearAdapter);

                    // January is position 1 because All months is position 0.
                    binding.spinnerMonth.setSelection(
                            today.getMonthValue()
                    );
                    binding.spinnerYear.setSelection(
                            years.indexOf(today.getYear())
                    );

                    selectorsReady = true;

                    binding.spinnerMonth.setEnabled(true);
                    binding.spinnerYear.setEnabled(true);

                    connectSelectorListeners();
                    loadMonthlyEvents();
                });

            } catch (Exception e) {
                AppExecutors.runOnMain(() -> {
                    if (binding == null || binding != currentBinding) {
                        return;
                    }

                    showLoadError();
                });
            }
        });
    }

    /**
     * Refreshes the report when the month or year changes.
     */
    private void connectSelectorListeners() {
        AdapterView.OnItemSelectedListener listener =
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {
                        loadMonthlyEvents();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // No selection to process.
                    }
                };

        binding.spinnerMonth.setOnItemSelectedListener(listener);
        binding.spinnerYear.setOnItemSelectedListener(listener);
    }

    /**
     * Loads events for one month or all months in the selected year.
     * Counts active registrations and calculates the report totals.
     */
    private void loadMonthlyEvents() {
        if (binding == null || !selectorsReady) {
            return;
        }

        // 0 means All months; 1–12 represent January–December.
        int month = binding.spinnerMonth.getSelectedItemPosition();
        int year = (Integer) binding.spinnerYear.getSelectedItem();
        int organizerID = session.getUserId();

        String yearMonth = String.format(
                Locale.ROOT, "%04d-%02d", year, month
        );

        int request = ++eventRequest;
        FragmentMonthlyEventsBinding currentBinding = binding;

        adapter.setEvents(new ArrayList<>(), new HashMap<>());
        binding.textEmpty.setVisibility(View.GONE);

        // Clear the old totals while loading the new selection.
        binding.textTotalEvents.setText("");
        binding.textTotalRegistrations.setText("");

        AppExecutors.run(() -> {
            try {
                List<Event> organizerEvents = new ArrayList<>();
                Map<Integer, Integer> counts = new HashMap<>();

                /*
                 * Read events and counts in one transaction so they
                 * represent the same database snapshot.
                 */
                db.runInTransaction(() -> {
                    List<Event> matchingEvents;

                    if (month == 0) {
                        matchingEvents = db.eventDao()
                                .getEventsByOrganizer(organizerID);
                    } else {
                        matchingEvents = db.eventDao()
                                .getMonthlyEvents(yearMonth);
                    }

                    String selectedYear = String.valueOf(year) + "-";

                    for (Event event : matchingEvents) {
                        if (event.getOrganizerID() == organizerID
                                && event.getEventDate()
                                .startsWith(selectedYear)) {

                            organizerEvents.add(event);

                            // The DAO excludes cancelled registrations.
                            int count = db.registrationDao()
                                    .countActiveRegistrations(
                                            event.getEventID()
                                    );

                            counts.put(event.getEventID(), count);
                        }
                    }
                });

                int registrationTotal = 0;

                for (int count : counts.values()) {
                    registrationTotal += count;
                }

                // Keep the result unchanged for the main-thread lambda.
                int totalRegistrations = registrationTotal;

                AppExecutors.runOnMain(() -> {
                    if (binding == null
                            || binding != currentBinding
                            || request != eventRequest) {
                        return;
                    }

                    adapter.setEvents(organizerEvents, counts);

                    binding.textTotalEvents.setText(
                            String.valueOf(organizerEvents.size())
                    );
                    binding.textTotalRegistrations.setText(
                            String.valueOf(totalRegistrations)
                    );

                    binding.textEmpty.setText(
                            R.string.message_no_monthly_events
                    );
                    binding.textEmpty.setVisibility(
                            organizerEvents.isEmpty()
                                    ? View.VISIBLE
                                    : View.GONE
                    );
                });

            } catch (Exception e) {
                AppExecutors.runOnMain(() -> {
                    if (binding == null
                            || binding != currentBinding
                            || request != eventRequest) {
                        return;
                    }

                    showLoadError();
                });
            }
        });
    }

    /**
     * Shows an error without displaying misleading totals.
     * Called only on the main thread.
     */
    private void showLoadError() {
        binding.textTotalEvents.setText("");
        binding.textTotalRegistrations.setText("");

        binding.textEmpty.setText(
                R.string.error_monthly_events_load
        );
        binding.textEmpty.setVisibility(View.VISIBLE);
    }

    /**
     * Refreshes events and counts after returning from Event Details.
     */
    @Override
    public void onResume() {
        super.onResume();

        if (selectorsReady) {
            loadMonthlyEvents();
        }
    }

    /**
     * Releases views and invalidates pending results for this screen.
     */
    @Override
    public void onDestroyView() {
        selectorsReady = false;
        eventRequest++;

        binding.spinnerMonth.setOnItemSelectedListener(null);
        binding.spinnerYear.setOnItemSelectedListener(null);
        binding.recyclerEvents.setAdapter(null);

        binding = null;
        adapter = null;

        super.onDestroyView();
    }
}