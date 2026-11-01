package com.example.eventnest;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.eventnest.data.database.AppDatabase;
import com.example.eventnest.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    AppDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = Room.databaseBuilder(
                        getApplicationContext(),
                        AppDatabase.class,
                        "EventNest.db"
                )
                .allowMainThreadQueries()
                .build();
        try {
            int eventCount = db.eventDao().getAllEvents().size();

            Toast.makeText(
                    this,
                    "Database connected. Events: " + eventCount,
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Database error: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}