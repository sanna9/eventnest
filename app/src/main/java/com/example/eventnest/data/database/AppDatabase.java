package com.example.eventnest.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

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
        version = 2
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract EventDao eventDao();
    public abstract RegistrationDao registrationDao();
    public abstract NotificationDao notificationDao();

    private static volatile AppDatabase instance;

    /*Upgrades an existing version 1 database to version 2
     * Adds Capacity without deleting existing records */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {

        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL(
                    "ALTER TABLE Events " +
                            "ADD COLUMN Capacity INTEGER NOT NULL DEFAULT 0"
            );
        }
    };

    /*Returns the shared database instance.
     * Creates it once and applies migrations when required */
    public static AppDatabase getInstance(Context context) {

        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {

                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "EventNest.db"
                            )
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }

        return instance;
    }
}