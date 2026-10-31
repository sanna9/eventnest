package com.example.myapplication.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "Users",
        indices = {
                @Index(value = {"Email"}, unique = true)
        }
)
public class User {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "UserID")
    private int userID;

    @ColumnInfo(name = "Name")
    private String name;

    @ColumnInfo(name = "Email")
    private String email;

    @ColumnInfo(name = "PasswordHash")
    private String passwordHash;

    @ColumnInfo(name = "Role")
    private String role;

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}