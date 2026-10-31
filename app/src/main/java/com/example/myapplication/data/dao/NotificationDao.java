package com.example.myapplication.data.dao;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.myapplication.data.entity.Notification;
import java.util.List;

@Dao
public interface NotificationDao {

    // Save one notification addressed to one student.
    // Later, the Send button will call this for each
    // active registered student in the selected event.
    @Insert
    void addNotification(Notification notification);

     // Retrieve messages for the signed-in student.
    // Show newest dates first.
    // For messages on the same date, show newest IDs first.
 
    @Query("SELECT * FROM Notifications " +
            "WHERE StudentID = :studentID " +
            "ORDER BY SentDate DESC, NotificationID DESC")
    List<Notification> getNotificationsByStudent(int studentID);
}
