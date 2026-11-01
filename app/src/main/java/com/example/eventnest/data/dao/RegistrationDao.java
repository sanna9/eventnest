package com.example.eventnest.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.eventnest.data.entity.Registration;
import com.example.eventnest.data.entity.User;
import java.util.List;

@Dao
public interface RegistrationDao {

//new register even status "active" before insert
    @Insert
    void addRegistration(Registration registration);


    @Query("SELECT * FROM Registrations " +
            "WHERE StudentID = :studentID AND EventID = :eventID")
    Registration getRegistration(int studentID, int eventID);

    //cancel registration ,but not history
    @Query("UPDATE Registrations SET Status = 'Cancelled' " +
            "WHERE StudentID = :studentID AND EventID = :eventID")
    int cancelRegistration(int studentID, int eventID);

    // Re-register by updating the EXISTING record.
    // Also record the new registration date.
    @Query("UPDATE Registrations " +
            "SET Status = 'Active', RegistrationDate = :registrationDate " +
            "WHERE StudentID = :studentID AND EventID = :eventID")
    int reactivateRegistration(
            int studentID,
            int eventID,
            String registrationDate
    );

    // date use yyyy-MM-dd.
    //Show current registrations
    @Query("SELECT Registrations.* FROM Registrations " +
            "INNER JOIN Events " +
            "ON Registrations.EventID = Events.EventID " +
            "WHERE Registrations.StudentID = :studentID " +
            "AND Registrations.Status = 'Active' " +
            "AND Events.EventDate >= :today " +
            "ORDER BY Events.EventDate, Events.EventTime")
    List<Registration> getCurrentRegistrations(
            int studentID,
            String today
    );

    //show past or cancelled registrations
    @Query("SELECT Registrations.* FROM Registrations " +
            "INNER JOIN Events " +
            "ON Registrations.EventID = Events.EventID " +
            "WHERE Registrations.StudentID = :studentID " +
            "AND (Events.EventDate < :today " +
            "OR Registrations.Status = 'Cancelled') " +
            "ORDER BY Events.EventDate DESC, Events.EventTime DESC")
    List<Registration> getRegistrationHistory(
            int studentID,
            String today
    );

    // get active registered students for event
    //use to fetch recipients when sending notifications
    //Organizer views students and sends messages
    @Query("SELECT Users.* FROM Users " +
            "INNER JOIN Registrations " +
            "ON Users.UserID = Registrations.StudentID " +
            "WHERE Registrations.EventID = :eventID " +
            "AND Registrations.Status = 'Active' " +
            "ORDER BY Users.Name")
    List<User> getRegisteredStudents(int eventID);
}