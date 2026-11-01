package com.example.myapplication.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "Registrations",
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "UserID",
                        childColumns = "StudentID"
                ),
                //delete event will remove its registration records
                @ForeignKey(
                        entity = Event.class,
                        parentColumns = "EventID",
                        childColumns = "EventID",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(
                        value = {"StudentID", "EventID"},
                        unique = true
                ),
                @Index(value = {"EventID"})
        }
)
public class Registration {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "RegistrationID")
    private int registrationID;
    @ColumnInfo(name = "StudentID")
    private int studentID;

    @ColumnInfo(name = "EventID")
    private int eventID;

    @ColumnInfo(name = "RegistrationDate")
    private String registrationDate;

    //"active or cancelled use case"
    @ColumnInfo(name = "Status")
    private String status;

    public int getRegistrationID() {
        return registrationID;
    }

    public void setRegistrationID(int registrationID) {
        this.registrationID = registrationID;
    }

    public int getStudentID() {
        return studentID;
    }

    public void setStudentID(int studentID) {
        this.studentID = studentID;
    }

    public int getEventID() {
        return eventID;
    }

    public void setEventID(int eventID) {
        this.eventID = eventID;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
