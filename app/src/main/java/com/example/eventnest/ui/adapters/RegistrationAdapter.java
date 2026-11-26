package com.example.eventnest.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventnest.R;
import com.example.eventnest.data.entity.Event;
import com.example.eventnest.databinding.ItemRegistrationBinding;
import com.example.eventnest.util.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Registration rows: CURRENT (Cancel button) or HISTORY (Past/Cancelled badge)
 */
public class RegistrationAdapter extends RecyclerView.Adapter<RegistrationAdapter.RegistrationViewHolder> {

    public enum Mode {CURRENT, HISTORY}

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public interface OnCancelClickListener {
        void onCancelClick(Event event);
    }

    private final Mode mode;
    private final OnEventClickListener rowListener;

    // pass null in HISTORY mode
    private final OnCancelClickListener cancelListener;
    private List<Event> events = new ArrayList<>();

    // only used in HISTORY mode
    private Set<Integer> cancelledIds = new HashSet<>();

    public RegistrationAdapter(Mode mode,
                               OnEventClickListener rowListener,
                               OnCancelClickListener cancelListener) {
        this.mode = mode;
        this.rowListener = rowListener;
        this.cancelListener = cancelListener;
    }

    public void setItems(List<Event> events, Set<Integer> cancelledIds) {
        this.events = events;
        this.cancelledIds = cancelledIds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RegistrationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRegistrationBinding binding = ItemRegistrationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RegistrationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RegistrationViewHolder holder, int position) {
        Event event = events.get(position);
        // Reuse binding from the current holder to avoid re-inflating
        ItemRegistrationBinding binding = holder.binding;
        binding.textEventTitle.setText(event.getTitle());
        binding.textEventLocation.setText(event.getLocation());
        binding.textEventMonth.setText(DateUtils.displayMonth(event.getEventDate()));
        binding.textEventDay.setText(DateUtils.displayDay(event.getEventDate()));
        binding.textEventDateTime.setText(
                new StringBuilder().append(DateUtils.displayDate(event.getEventDate())).append(" - ")
                        .append(DateUtils.displayTime(event.getEventTime())).toString());
        binding.getRoot().setOnClickListener(v -> rowListener.onEventClick(event));

        if (mode == Mode.CURRENT) {
            // Available state, show the cancel button
            binding.textBadge.setVisibility(View.GONE);
            binding.buttonCancel.setVisibility(View.VISIBLE);
            // TODO: implement cancel
            binding.buttonCancel.setOnClickListener( v -> cancelListener.onCancelClick(event));
        } else {
            // History state, show Cancelled or Past text
            binding.buttonCancel.setVisibility(View.GONE);
            binding.textBadge.setVisibility(View.VISIBLE);

            // Display status badge based on status of the event (past or cancelled)
            boolean cancelled = cancelledIds.contains(event.getEventID());
            binding.textBadge.setText(cancelled ? R.string.badge_cancelled : R.string.badge_past);
            binding.textBadge.setTextColor(ContextCompat
                    .getColor(holder.itemView.getContext(),
                    cancelled ? R.color.eventnest_error : R.color.eventnest_text_secondary
            ));
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class RegistrationViewHolder extends RecyclerView.ViewHolder {
        final ItemRegistrationBinding binding;

        public RegistrationViewHolder(ItemRegistrationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
