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
import com.example.eventnest.data.dao.RegistrationDao;
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
import java.util.Set;

public class RegistrationHistoryFragment extends Fragment {

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

        binding.textEmpty.setText(R.string.empty_history);

        // This fragment is for history checking which is located on student's side bottom tab
        adapter = new RegistrationAdapter(RegistrationAdapter.Mode.HISTORY,
                event -> openDetails(event), null);

        binding.recyclerRegistrations.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerRegistrations.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
    }

    /**
     * Loads the user's event history and cancelled ids then renders the list
     * Set here for avoiding duplicate ids
     */
    private void loadHistory() {
        FragmentRegistrationListBinding requestBinding = binding;
        int studentId = session.getUserId();
        String today = DateUtils.today();

        AppExecutors.run(() -> {
            try {
                RegistrationDao dao = db.registrationDao();
                List<Event> events = dao.getHistoryEvents(studentId, today);
                Set<Integer> cancelledIds = new HashSet<>(dao.getCancelledEventIDs(studentId));

                AppExecutors.runOnMain(() -> showHistory(requestBinding, events, cancelledIds));
            } catch (Exception e) {
                AppExecutors.runOnMain(() -> showError(requestBinding));
            }
        });
    }

    private void showHistory(FragmentRegistrationListBinding requestBinding,
                             List<Event> events, Set<Integer> cancelledIds) {
        if (binding == null || binding != requestBinding) return;

        adapter.setItems(events, cancelledIds);
        binding.textEmpty.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showError(FragmentRegistrationListBinding requestBinding) {
        if (binding == null || binding != requestBinding) return;
        Toast.makeText(requireContext(), R.string.error_load_events, Toast.LENGTH_LONG).show();
    }

    private void openDetails(Event event) {
        Intent intent = new Intent(requireContext(), EventDetailsActivity.class);
        intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, event.getEventID());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        binding.recyclerRegistrations.setAdapter(null);
        binding = null;
        adapter = null;
        super.onDestroyView();
    }
}
