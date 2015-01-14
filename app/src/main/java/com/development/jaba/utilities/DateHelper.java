package com.development.jaba.utilities;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Static class containing some utility methods.
 */
public class DateHelper {
    /**
     * Converts a {@link java.util.Date} object into a date and time string.
     *
     * @param date The {@link java.util.Date} object to convert.
     * @return A date and time string using the yyyy-MM-dd HH:mm:ss format.
     */
    public static String toDateTimeString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(date);
    }

    /**
     * Converts a {@link java.util.Date} object to a month and year string.
     *
     * @param date The {@link java.util.Date} object to convert.
     * @return A string formatted using the 'LLLL yyyy' format.
     */
    public static String toMonthYearString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("LLLL yyyy", Locale.getDefault());
        return dateFormat.format(date);
    }

    /**
     * Converts a given month number to the month name using the default
     * {@link java.util.Locale}.
     *
     * @param month The month (0-11).
     * @return The month name.
     */
    public static String toMonthNameString(int month) throws ArrayIndexOutOfBoundsException {
        return DateFormatSymbols.getInstance(Locale.getDefault()).getMonths()[month];
    }

    /**
     * Gets the year from a given date.
     *
     * @param date The {@link java.util.Date} object to get the year from.
     * @return The year from the date of -1 in case of an error (null Date)
     */
    public static int getYearFromDate(Date date) {
        int result = -1;
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            result = cal.get(Calendar.YEAR);
        }
        return result;
    }

    /**
     * Converts the given {@link java.util.Date} to a key string containing only the
     * year and the month.
     *
     * @param date The {@link java.util.Date} to convert.
     * @return The key string using the format 'yyyy-MM'
     */
    public static String toMonthYearKey(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        return dateFormat.format(date);
    }


    /**
     * Converts a date and time string in the form yyyy-MM-dd HH:mm:ss to a
     * Date object.
     *
     * @param dateString The date and time string in the form yyyy-MM-dd HH:mm:ss to convert to a {@link java.util.Date} object.
     * @return The {@link java.util.Date} object or null in case of a parsing error.
     */
    public static Date fromDateTimeString(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts a month year key string previously created with {@see #toMonthYearKey} into a
     * {@link java.util.Date} object. The day of this date is set to 1 and the hour, minute, seconds
     * and milliseconds to 0.
     *
     * @param key The month year key previously created with {@see #toMonthYearKey}.
     * @return The {@link java.util.Date} object or null in case of a parsing failure.
     */
    public static Date fromMonthYearKey(String key) {
        Calendar cal = Calendar.getInstance();
        String[] parts = key.split("-");
        if (parts.length == 2) {
            cal.set(Calendar.YEAR, Integer.parseInt(parts[0]));
            cal.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();
        }
        return null;
    }
}
