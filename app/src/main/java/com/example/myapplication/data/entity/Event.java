package com.example.myapplication.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "Events",
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "UserID",
                        childColumns = "OrganizerID"
                )
        },
        indices = {
                @Index(value = {"OrganizerID"})
        }
)
public class Event {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "EventID")
    private int eventID;

    @ColumnInfo(name = "OrganizerID")
    private int organizerID;

    @ColumnInfo(name = "Title")
    private String title;

    @ColumnInfo(name = "Description")
    private String description;

    @ColumnInfo(name = "EventDate")
    private String eventDate;

    @ColumnInfo(name = "EventTime")
    private String eventTime;

    @ColumnInfo(name = "Location")
    private String location;

//getter setters
    public int getEventID() {
        return eventID;
    }

    public void setEventID(int eventID) {
        this.eventID = eventID;
    }

    public int getOrganizerID() {
        return organizerID;
    }

    public void setOrganizerID(int organizerID) {
        this.organizerID = organizerID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
