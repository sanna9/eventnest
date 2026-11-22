package com.example.eventnest.ui.organizer;

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

import com.example.eventnest.data.database.AppDatabase;
import com.example.eventnest.data.entity.Event;
import com.example.eventnest.databinding.FragmentEventListBinding;
import com.example.eventnest.ui.adapters.EventAdapter;
import com.example.eventnest.ui.shared.EventDetailsActivity;
import com.example.eventnest.util.AppExecutors;
import com.example.eventnest.R;

import java.util.List;

public class EventListFragment extends Fragment {

    private FragmentEventListBinding binding;
    private AppDatabase db;
    private EventAdapter adapter;

    /**
     * Creates the Fragment layout using ViewBinding.
     */
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentEventListBinding.inflate(
                inflater,
                container,
                false
        );

        return binding.getRoot();
    }

    /**
     * Connects the RecyclerView, adapter and button actions.
     */
    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        db = AppDatabase.getInstance(requireContext());

        // fragment decides what happens when a row is clicked
        // Open detail page for the selected event
        adapter = new EventAdapter(event -> {
            Intent intent = new Intent(
                    requireContext(),
                    EventDetailsActivity.class
            );

            intent.putExtra("event_id", event.getEventID());
            startActivity(intent);
        });

        binding.recyclerEvents.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        binding.recyclerEvents.setAdapter(adapter);

        // Return through the Activity's navigation history.
        binding.buttonBack.setOnClickListener(v ->
                requireActivity()
                        .getOnBackPressedDispatcher()
                        .onBackPressed()
        );


        // Open the Create Event screen
        binding.buttonCreateEvent.setOnClickListener(v -> {
            Intent intent = new Intent(
                    requireContext(),
                    EventFormActivity.class
            );
            startActivity(intent);
        });
    }

    /**
     * Refreshes the list whenever this Fragment becomes active.
     * This will also show changes after returning from event editing.
     */
    @Override
    public void onResume() {
        super.onResume();
        loadEvents();
    }

    /**
     * Reads events on the background thread.
     * Updates the RecyclerView on the main thread.
     */
    private void loadEvents() {

        // Remember which Fragment view requested this data.
        FragmentEventListBinding currentBinding = binding;

        AppExecutors.run(() -> {
            try {
                List<Event> events = db.eventDao().getAllEvents();

                AppExecutors.runOnMain(() -> {

                    // Ignore the result if this view has been destroyed.
                    if (binding == null || binding != currentBinding) {
                        return;
                    }

                    adapter.setEvents(events);

                    binding.textEmpty.setVisibility(
                            events.isEmpty() ? View.VISIBLE : View.GONE
                    );
                });

            } catch (Exception e) {

                AppExecutors.runOnMain(() -> {
                    if (binding == null || binding != currentBinding) {
                        return;
                    }

                    Toast.makeText(
                            binding.getRoot().getContext(),
                            getString(R.string.error_events_load),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    /**
     * Releases references to the Fragment's views when they are destroyed.
     */
    @Override
    public void onDestroyView() {
        binding.recyclerEvents.setAdapter(null);
        binding = null;
        adapter = null;

        super.onDestroyView();
    }
}