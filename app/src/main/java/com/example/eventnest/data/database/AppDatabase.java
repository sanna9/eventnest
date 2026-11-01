package com.example.eventnest.data.database;
import androidx.room.Database;
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
}
