package com.example.eventnest.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventnest.data.entity.User;
import com.example.eventnest.databinding.ItemRegisteredStudentBinding;

import java.util.ArrayList;
import java.util.List;

public class RegisteredStudentAdapter extends RecyclerView.Adapter<
        RegisteredStudentAdapter.StudentViewHolder> {

    List<User> studentList = new ArrayList<>();

    /**
     * Receives the registered students and refreshes the list.
     */
    public void setStudents(List<User> list) {
        studentList = list;
        notifyDataSetChanged();
    }

    /**
     * Creates one student row using ViewBinding.
     */
    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ItemRegisteredStudentBinding binding =
                ItemRegisteredStudentBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                );

        return new StudentViewHolder(binding);
    }

    /**
     * Displays the student's name and email in the row.
     */
    @Override
    public void onBindViewHolder(
            @NonNull StudentViewHolder holder,
            int position
    ) {
        User student = studentList.get(position);

        holder.binding.textStudentName.setText(student.getName());
        holder.binding.textStudentEmail.setText(student.getEmail());
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    /**
     * Holds the binding for one reusable student row.
     */
    class StudentViewHolder extends RecyclerView.ViewHolder {

        ItemRegisteredStudentBinding binding;

        public StudentViewHolder(
                ItemRegisteredStudentBinding itemBinding
        ) {
            super(itemBinding.getRoot());
            binding = itemBinding;
        }
    }
}