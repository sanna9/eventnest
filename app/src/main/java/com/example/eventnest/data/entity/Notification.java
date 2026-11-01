package com.example.eventnest.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
@Entity(
        tableName = "Notifications",
        foreignKeys = {
                @ForeignKey(
                        entity = Event.class,
                        parentColumns = "EventID",
                        childColumns = "EventID",
                        onDelete = ForeignKey.CASCADE
                ),

                @ForeignKey(
                        entity = User.class,
                        parentColumns = "UserID",
                        childColumns = "StudentID"
                )
        },
        indices = {
                @Index(value = {"EventID"}),
                @Index(value = {"StudentID"})
        }
)
public class Notification {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "NotificationID")
    private int notificationID;
    @ColumnInfo(name = "EventID")
    private int eventID;

    @ColumnInfo(name = "StudentID")
    private int studentID;

    @ColumnInfo(name = "Message")
    private String message;
    @ColumnInfo(name = "SentDate")
    private String sentDate;

    public int getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(int notificationID) {
        this.notificationID = notificationID;
    }

    public int getEventID() {
        return eventID;
    }

    public void setEventID(int eventID) {
        this.eventID = eventID;
    }

    public int getStudentID() {
        return studentID;
    }

    public void setStudentID(int studentID) {
        this.studentID = studentID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSentDate() {
        return sentDate;
    }

    public void setSentDate(String sentDate) {
        this.sentDate = sentDate;
    }
}
