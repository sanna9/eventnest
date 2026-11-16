package com.example.eventnest.ui.student;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class StudentPlaceholderFragment extends Fragment {
    private static final String ARG_TEXT = "text";


    /**
     *  Factory pattern + Bundle for safe data passing,
     *   ensuring data is preserved across configuration changes like screen rotations
     */
    public static StudentPlaceholderFragment newInstance(String text) {
        StudentPlaceholderFragment placeholderFragment = new StudentPlaceholderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEXT, text);
        placeholderFragment.setArguments(args);
        return placeholderFragment;
    }

    /**
     * Apparently I'm just putting Text here first so I can track which tabs currently I'm in
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     */

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        TextView view = new TextView(requireContext());
        view.setGravity(Gravity.CENTER);
        view.setText(requireArguments().getString(ARG_TEXT));
        return view;
    }
}