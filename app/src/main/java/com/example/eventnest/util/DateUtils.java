package com.example.eventnest.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

// Requires SDK 26+
public class DateUtils {
    public DateUtils() {}

/**
 * utils for formatting date, skip Locale,
 * current format date is yyyy-MM-dd and time is 00:00 (24 hours)
 * @param year Year to format (ex: 2026)
 * @param month Month to format **DatePickerDialog is using zero based so we have to +1
 * @param day Day of the month
 * @return formatted day (2026-10-03) (DatePickerDialog will pick 9 as October).
 *
 * @sample
 * DateUtils.formatDate(2026, 4, 9) returns 2026-05-09
 */

    public static String formatDate(int year, int month, int day) {
        return LocalDate.of(year, month+1, day).toString();
    }

    public static String formatTime(int hour, int minute) {
        return LocalTime.of(hour, minute).format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public static String today() {
        return LocalDate.now().toString();
    }

/**
 * convert date from 2026-10-10 into "Oct 10, 2026"
 */
    public static String displayDate(String date) {
        if (date == null || date.isEmpty()) {
            return date;
        }
        try {
            LocalDate dateDisplay = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
            // Locale.getDefault will display based on user's devices
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault());
            return dateDisplay.format(formatter);
        } catch (DateTimeParseException e) {
            return date;
        }
    }

    /**
     * Returns the short month for the event's date badge.
     * Example: "2026-11-15" becomes "NOV".
     */
    public static String displayMonth(String date) {
        if (date == null || date.isEmpty()) {
            return "";
        }

        try {
            return LocalDate.parse(date)
                    .format(DateTimeFormatter.ofPattern(
                            "MMM", Locale.getDefault()
                    ))
                    .toUpperCase(Locale.getDefault());

        } catch (DateTimeParseException e) {
            return "";
        }
    }

    /**
     * Returns the day number for the event's date badge.
     * Example: "2026-11-15" becomes "15".
     */
    public static String displayDay(String date) {
        if (date == null || date.isEmpty()) {
            return "";
        }

        try {
            return LocalDate.parse(date)
                    .format(DateTimeFormatter.ofPattern("dd"));

        } catch (DateTimeParseException e) {
            return "";
        }
    }

    /**
     * Converts stored 24-hour time into readable time.
     * Example: "14:30" becomes "2:30 PM" with an English locale.
     */
    public static String displayTime(String time) {
        if (time == null || time.isEmpty()) {
            return "";
        }

        try {
            return LocalTime.parse(time)
                    .format(DateTimeFormatter.ofPattern(
                            "h:mm a", Locale.getDefault()
                    ));

        } catch (DateTimeParseException e) {
            return time;
        }
    }

    public static String daysFromToday(int days) {
        return LocalDate.now().plusDays(days).toString();
    }
}
