package com.example.eventnest;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import com.example.eventnest.data.database.AppDatabase;
import com.example.eventnest.databinding.ActivityMainBinding;
import com.example.eventnest.util.OrganizerSetup;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    AppDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(
                binding.getRoot(), (v, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );
        db = Room.databaseBuilder(
                        getApplicationContext(),
                        AppDatabase.class,
                        "EventNest.db"
                )
                .allowMainThreadQueries()
                .build();
        OrganizerSetup.createOrganizerIfNeeded(db);
    }
}