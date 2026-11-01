package com.example.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myapplication.data.entity.User;
@Dao
public interface UserDao {
    @Insert
    void addUser(User user);
    @Query("SELECT * FROM Users WHERE Email = :email LIMIT 1")
    User getUserByEmail(String email);
    @Query("SELECT * FROM Users WHERE UserID = :id")
    User getUserByID(int id);
}