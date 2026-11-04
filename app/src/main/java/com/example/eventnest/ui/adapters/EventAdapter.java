package com.example.eventnest.ui.adapters;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventnest.data.entity.Event;
import com.example.eventnest.databinding.ItemEventBinding;
import com.example.eventnest.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter
        extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<Event> eventList = new ArrayList<>();
    private final OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public EventAdapter(OnEventClickListener listener) {
        this.listener = listener;
    }

    /**
     * Receives event records from the Fragment and refreshes list.
     * Call this on the main thread after loading data from Room.
     */
    public void setEvents(List<Event> events) {
        eventList = events;
        notifyDataSetChanged();
    }

    /**
     * Creates one event row using the binding for item_event.xml.
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ItemEventBinding binding = ItemEventBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        return new EventViewHolder(binding);
    }

    /**
     * Displays an event in the row and attaches its click action.
     * Reuses DateUtils for date and time formatting.
     */
    @Override
    public void onBindViewHolder(
            @NonNull EventViewHolder holder,
            int position
    ) {
        Event event = eventList.get(position);

        holder.binding.textEventTitle.setText(event.getTitle());
        holder.binding.textEventLocation.setText(event.getLocation());

        // Format the event date and time using our shared utility.
        holder.binding.textEventMonth.setText(
                DateUtils.displayMonth(event.getEventDate())
        );

        holder.binding.textEventDay.setText(
                DateUtils.displayDay(event.getEventDate())
        );

        holder.binding.textEventDateTime.setText(
                DateUtils.displayDate(event.getEventDate())
                        + " • "
                        + DateUtils.displayTime(event.getEventTime())
        );

        holder.binding.getRoot().setOnClickListener(v ->
                listener.onEventClick(event)
        );
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * Holds the ViewBinding for one event row
     * RecyclerView reuses these rows while scrolling
     */
    public static class EventViewHolder extends RecyclerView.ViewHolder {

        final ItemEventBinding binding;

        public EventViewHolder(ItemEventBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}