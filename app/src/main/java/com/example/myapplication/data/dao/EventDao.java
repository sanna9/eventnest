package com.example.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.data.entity.Event;

import java.util.List;

@Dao
public interface EventDao {
    @Insert
    void addEvent(Event event);

    // dates yyyy-MM-dd and time HH:mm for correct sorting
    @Query("SELECT * FROM Events ORDER BY EventDate, EventTime")
    List<Event> getAllEvents();

    @Query("SELECT * FROM Events WHERE EventID = :id")
    Event getEventByID(int id);

    @Query("SELECT * FROM Events WHERE OrganizerID = :organizerID " +
            "ORDER BY EventDate, EventTime")
    List<Event> getEventsByOrganizer(int organizerID);

    @Update
    void updateEvent(Event event);
    @Query("DELETE FROM Events WHERE EventID = :id")
    int deleteEvent(int id);

    @Query("SELECT * FROM Events " +
            "WHERE EventDate LIKE :yearMonth || '-%' " +
            "ORDER BY EventDate, EventTime")
    List<Event> getMonthlyEvents(String yearMonth);
}