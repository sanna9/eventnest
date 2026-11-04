package com.example.eventnest.data.database;
import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.eventnest.data.dao.UserDao;
import com.example.eventnest.data.dao.EventDao;
import com.example.eventnest.data.dao.RegistrationDao;
import com.example.eventnest.data.dao.NotificationDao;

import com.example.eventnest.data.entity.User;
import com.example.eventnest.data.entity.Event;
import com.example.eventnest.data.entity.Registration;
import com.example.eventnest.data.entity.Notification;

@Database(
        entities = {
                User.class,
                Event.class,
                Registration.class,
                Notification.class
        },
        version = 1
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract EventDao eventDao();
    public abstract RegistrationDao registrationDao();
    public abstract NotificationDao notificationDao();

    /**
     *  Singleton pattern for performance optimize
     *  volatile for preventing thread-local caching issues where changes made or
     *  double-checked locking
     */
    private static volatile AppDatabase instance;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "EventNest.db"
                    ).build();
                }
            }
        }
        return instance;
    }
}
