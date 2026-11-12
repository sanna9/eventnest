package com.example.eventnest.util;
import com.example.eventnest.data.dao.EventDao;
import com.example.eventnest.data.dao.NotificationDao;
import com.example.eventnest.data.dao.RegistrationDao;
import com.example.eventnest.data.dao.UserDao;
import com.example.eventnest.data.database.AppDatabase;
import com.example.eventnest.data.entity.Event;
import com.example.eventnest.data.entity.Notification;
import com.example.eventnest.data.entity.Registration;
import com.example.eventnest.data.entity.User;
import com.example.eventnest.data.model.Role;

public class DatabaseSeeder {
    private static final boolean SEED_DEMO_DATA = true;

    public static final String ORGANIZER_EMAIL = "organizer@eventnest.com";
    public static final String ORGANIZER_PASSWORD = "EventNest@123";
    public static final String STUDENT_EMAIL = "student@eventnest.com";
    public static final String STUDENT_PASSWORD = "Student@123";

    private static final String ACTIVE = "Active";
    private static final String CANCELLED = "Cancelled";

    public static void seed(AppDatabase db) {
        db.runInTransaction(() -> {
            int organizerId = seedOrganizer(db);
            if (SEED_DEMO_DATA) {
                seedDemoData(db, organizerId);
            }
        });
    }

    /**
     * all events created by 1 organizer at first
     */
    private static int seedOrganizer(AppDatabase db) {
        UserDao userDao = db.userDao();
        User existing = userDao.getUserByEmail(ORGANIZER_EMAIL);
        if (existing != null) {
            return existing.getUserID();
        }
        return addUser(userDao, "EventNest Organizer", ORGANIZER_EMAIL,
                ORGANIZER_PASSWORD, Role.ORGANIZER, true);
    }

    private static void seedDemoData(AppDatabase db, int organizerId) {
        UserDao userDao = db.userDao();
        if (userDao.getUserByEmail(STUDENT_EMAIL) != null) {
            return;
        }

        // Students
        int demo = addUser(userDao, "Demo Student", STUDENT_EMAIL, STUDENT_PASSWORD, Role.STUDENT,true);
        int jane = addUser(userDao, "Jane Doe", "jane.doe@student.edu", STUDENT_PASSWORD, Role.STUDENT,true);
        int adam = addUser(userDao, "Adam Smith", "adam.smith@student.edu", STUDENT_PASSWORD, Role.STUDENT,true);
        int duc = addUser(userDao, "Duc Pham", "duc.pham@student.edu", STUDENT_PASSWORD, Role.STUDENT,true);
        int ha = addUser(userDao, "Ha Vo", "ha.vo@student.edu", STUDENT_PASSWORD, Role.STUDENT,true);
        int lan = addUser(userDao, "Lan Bui", "lan.bui@student.edu", STUDENT_PASSWORD, Role.STUDENT,true);

        // Deactivated account
        addUser(userDao, "Old Account", "deactivated@student.edu", STUDENT_PASSWORD, Role.STUDENT, false);

        //        TODO: Extra organizers (demo only)
        //        int org2 = addUser(userDao, "Science Club Organizer", "organizer2@eventnest.com",
        //                ORGANIZER_PASSWORD, Role.ORGANIZER, true);
        //        int org3 = addUser(userDao, "Sports Dept Organizer", "organizer3@eventnest.com",
        //                ORGANIZER_PASSWORD, Role.ORGANIZER, true);


        // Events (dates are relative to today)
        EventDao eventDao = db.eventDao();
        int welcome = addEvent(eventDao, organizerId, "Welcome Week Orientation",
                "Meet clubs, staff and new friends.", -20, "10:00", "Main Hall");
        int career = addEvent(eventDao, organizerId, "Career Fair",
                "Talk to local employers about co-op and jobs.", -7, "13:00", "Gym A");
        int hackathon = addEvent(eventDao, organizerId, "Hackathon 2026",
                "24-hour team coding challenge. Bring your laptop.", 2, "09:00", "Room B201");
        int photo = addEvent(eventDao, organizerId, "Photography Workshop",
                "Basics of composition and lighting.", 5, "15:30", "Art Studio 3");
        int study = addEvent(eventDao, organizerId, "Study Skills Seminar",
                "Time management and exam preparation tips.", 9, "12:00", "Library Room 2");
        int movie = addEvent(eventDao, organizerId, "Movie Night",
                "Free screening with snacks.", 14, "19:00", "Student Lounge");
        int volunteer = addEvent(eventDao, organizerId, "Volunteer Day",
                "Help with campus clean-up and the food drive.", 20, "09:30", "Courtyard");

        // No registrations on purpose: tests the empty state.
        addEvent(eventDao, organizerId, "Winter Concert",
                "Student orchestra and choir performance.", 35, "18:30", "Auditorium");

        // Registrations
        // past event -> History
        // cancelled -> History, can re-register
        RegistrationDao regDao = db.registrationDao();
        addRegistration(regDao, demo, hackathon, 3, ACTIVE);
        addRegistration(regDao, demo, photo, 2, ACTIVE);
        addRegistration(regDao, demo, career, 14, ACTIVE);
        addRegistration(regDao, demo, movie, 5, CANCELLED);
        addRegistration(regDao, jane, hackathon, 4, ACTIVE);
        addRegistration(regDao, jane, career, 15, ACTIVE);
        addRegistration(regDao, jane, photo, 1, ACTIVE);
        addRegistration(regDao, adam, hackathon, 3, ACTIVE);
        addRegistration(regDao, adam, welcome, 25, ACTIVE);
        addRegistration(regDao, duc, hackathon, 2, ACTIVE);
        addRegistration(regDao, duc, study, 1, ACTIVE);
        addRegistration(regDao, ha, hackathon, 1, ACTIVE);
        addRegistration(regDao, ha, study, 1, CANCELLED);
        addRegistration(regDao, lan, photo, 1, ACTIVE);
        addRegistration(regDao, lan, volunteer, 1, ACTIVE);

        NotificationDao notificationDao = db.notificationDao();
        // Like the organizer pressing "Send" for every active student of the hackathon.
        for (int studentId : new int[]{demo, jane, adam, duc, ha}) {
            addNotification(notificationDao, hackathon, studentId,
                    "Reminder: Hackathon 2026 starts in 2 days. Bring your laptop!", 0);
        }
        addNotification(notificationDao, photo, demo,
                "Photography Workshop: please bring a camera or a phone.", 1);
        addNotification(notificationDao, career, demo,
                "Thanks for attending the Career Fair!", 6);
    }

    /**
     * Utils method for seeding data
     * @param dao
     * @param name
     * @param email
     * @param password
     * @param role
     * @param active
     * @return
     */
    private static int addUser(UserDao dao, String name, String email,
                               String password, Role role, boolean active) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(PasswordUtils.hashPassword(password));
        user.setRole(role);
        user.setActive(active);
        return (int) dao.addUser(user);
    }

    private static int addEvent(EventDao dao, int organizerId, String title,
                                String description, int daysFromToday,
                                String time, String location) {
        Event event = new Event();
        event.setOrganizerID(organizerId);
        event.setTitle(title);
        event.setDescription(description);
        event.setEventDate(DateUtils.daysFromToday(daysFromToday));
        event.setEventTime(time);
        event.setLocation(location);
        return (int) dao.addEvent(event);
    }

    private static void addRegistration(RegistrationDao dao, int studentId,
                                        int eventId, int daysAgo, String status) {
        Registration registration = new Registration();
        registration.setStudentID(studentId);
        registration.setEventID(eventId);
        registration.setRegistrationDate(DateUtils.daysFromToday(-daysAgo));
        registration.setStatus(status);
        dao.addRegistration(registration);
    }

    private static void addNotification(NotificationDao dao, int eventId,
                                        int studentId, String message, int daysAgo) {
        Notification notification = new Notification();
        notification.setEventID(eventId);
        notification.setStudentID(studentId);
        notification.setMessage(message);
        notification.setSentDate(DateUtils.daysFromToday(-daysAgo));
        dao.addNotification(notification);
    }
}
