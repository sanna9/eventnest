package com.example.eventnest.ui.student;

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
import com.example.eventnest.databinding.FragmentAvailableEventsBinding;
import com.example.eventnest.ui.adapters.EventAdapter;
import com.example.eventnest.ui.shared.EventDetailsActivity;
import com.example.eventnest.util.AppExecutors;
import com.example.eventnest.util.DateUtils;

import java.util.List;

public class AvailableEventsFragment extends Fragment {

    private FragmentAvailableEventsBinding binding;
    private AppDatabase db;
    private EventAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAvailableEventsBinding.inflate(inflater, container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = AppDatabase.getInstance(requireContext());

        adapter = new EventAdapter(event -> onEventClicked(event));

        binding.recyclerEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerEvents.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEvents();
    }

    /**
     * Loads upcoming events from the database on a background thread and updates
     * the UI on the main thread,
     * the user might leave or reopen the screen while the query is still running,
     * we capture {@code requestBinding} now and pass it to the UI callbacks
     * to ignore stale results if the view was destroyed or replaced.
     * If the user leaves and returns while loading view A, a new view B is created. When
     * view A's query finishes late, it no longer matches {@code binding} and is ignored.
     */
    private void loadEvents() {
        FragmentAvailableEventsBinding requestBinding = binding;
        String today = DateUtils.today();
        // Background thread
        AppExecutors.run(() -> {
            try {
                List<Event> events = db.eventDao().getUpComingEvent(today);
                AppExecutors.runOnMain(() -> showEvents(requestBinding, events));
            } catch (Exception e) {
                AppExecutors.runOnMain(() -> showLoadError(requestBinding));
            }
        });
    }

    private void showEvents(FragmentAvailableEventsBinding requestBinding, List<Event> events) {
        // the user may have left this screen while the database was loading
        // If the view is gone (or is a newer one), do nothing
        if (binding == null || binding != requestBinding) return;

        adapter.setEvents(events);

        if (events.isEmpty()) {
            binding.textEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.textEmpty.setVisibility(View.GONE);
        }
    }

    /**
     * Displays an error Toast if loading upcoming events fails while the original
     * view binding is still active
     *
     * @param requestBinding View binding captured when the load request init,
     *                       used to ignore errors if the view was destroyed or recreated.
     */
    private void showLoadError(FragmentAvailableEventsBinding requestBinding) {
        if (binding == null || binding != requestBinding) return;
        Toast.makeText(requireContext(), R.string.error_load_events, Toast.LENGTH_LONG).show();
    }

    private void onEventClicked(Event event) {
        Intent intent = new Intent(
                requireContext(),
                EventDetailsActivity.class
        );
        intent.putExtra("event_id", event.getEventID());
        startActivity(intent);
    }

    /**
     * Clean up and detach adapter
     */
    @Override
    public void onDestroyView() {
        binding.recyclerEvents.setAdapter(null);
        binding = null;
        adapter = null;
        super.onDestroyView();
    }
}
