package com.example.myapplication.data.database;
import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.myapplication.data.dao.UserDao;
import com.example.myapplication.data.dao.EventDao;
import com.example.myapplication.data.dao.RegistrationDao;
import com.example.myapplication.data.dao.NotificationDao;

import com.example.myapplication.data.entity.User;
import com.example.myapplication.data.entity.Event;
import com.example.myapplication.data.entity.Registration;
import com.example.myapplication.data.entity.Notification;

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
