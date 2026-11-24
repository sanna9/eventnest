package com.example.eventnest.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventnest.data.entity.Event;
import com.example.eventnest.databinding.ItemMonthlyEventBinding;
import com.example.eventnest.util.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class MonthlyEventAdapter extends RecyclerView.Adapter<
        MonthlyEventAdapter.MonthlyEventViewHolder> {

    List<Event> eventList = new ArrayList<>();

    // Stores each registration count using its EventID.
    Map<Integer, Integer> registrationCounts = new HashMap<>();

    OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public MonthlyEventAdapter(OnEventClickListener listener) {
        this.listener = listener;
    }

    /*Receives events and their active registration counts.
     * The Fragment calls this on the main thread after reading Room */
    public void setEvents(
            List<Event> events,
            Map<Integer, Integer> counts
    ) {
        eventList = events;
        registrationCounts = counts;
        notifyDataSetChanged();
    }

    /**Creates one monthly event row using ViewBinding*/
    @NonNull
    @Override
    public MonthlyEventViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ItemMonthlyEventBinding binding =
                ItemMonthlyEventBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                );

        return new MonthlyEventViewHolder(binding);
    }

    /** Displays event details and its active registration count.
     * Reuses DateUtils to format the month and day */
    @Override
    public void onBindViewHolder(
            @NonNull MonthlyEventViewHolder holder,
            int position
    ) {
        Event event = eventList.get(position);

        holder.binding.textEventMonth.setText(
                DateUtils.displayMonth(event.getEventDate())
        );

        holder.binding.textEventDay.setText(
                DateUtils.displayDay(event.getEventDate())
        );

        holder.binding.textEventTitle.setText(event.getTitle());
        holder.binding.textEventLocation.setText(event.getLocation());

        Integer count = registrationCounts.get(event.getEventID());

        if (count == null) {
            count = 0;
        }

        holder.binding.textRegisteredCount.setText(
                String.valueOf(count)
        );

        holder.binding.getRoot().setOnClickListener(v ->
                listener.onEventClick(event)
        );
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
    /**Holds the binding for a row that RecyclerView can reuse.*/
    class MonthlyEventViewHolder extends RecyclerView.ViewHolder {

        ItemMonthlyEventBinding binding;

        public MonthlyEventViewHolder(
                ItemMonthlyEventBinding itemBinding
        ) {
            super(itemBinding.getRoot());
            binding = itemBinding;
        }
    }
}