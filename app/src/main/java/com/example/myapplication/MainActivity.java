package com.example.myapplication;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import com.example.myapplication.data.database.AppDatabase;
import com.example.myapplication.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // Binding gives access to views in activity_main.xml.
    ActivityMainBinding binding;

    // Holds the Room database connection.
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
        // Temporary check: run a query to open and verify the database.
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