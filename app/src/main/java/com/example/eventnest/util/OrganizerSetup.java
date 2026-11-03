package com.example.eventnest.util;
import com.example.eventnest.data.dao.UserDao;
import com.example.eventnest.data.database.AppDatabase;
import com.example.eventnest.data.entity.User;

public class OrganizerSetup {
    public static final String ORGANIZER_EMAIL =
            "organizer@eventnest.com";
    public static void createOrganizerIfNeeded(AppDatabase db) {
        UserDao userDao = db.userDao();
        User existingUser = userDao.getUserByEmail(ORGANIZER_EMAIL);
        if (existingUser == null) {
            User organizer = new User();
            organizer.setName("EventNest Organizer");
            organizer.setEmail(ORGANIZER_EMAIL);
            // Hash the password before saving it.
            organizer.setPasswordHash(
                    PasswordUtils.hashPassword("EventNest@123")
            );
            organizer.setRole("Organizer");
            userDao.addUser(organizer);
        }
    }
}
