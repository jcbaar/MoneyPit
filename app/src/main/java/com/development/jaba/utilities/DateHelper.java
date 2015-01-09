package com.development.jaba.utilities;

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
     * Converts a Date object into a time string.
     * @param date The Date object to convert.
     * @return A DateTime string in the form yyyy-MM-dd HH:mm:ss
     */
    public static String getDateTimeAsString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(date);
    }

    /**
     * Converts a DateTime string in the form yyyy-MM-dd HH:mm:ss to a
     * Date object.
     * @param dateString The DateTime string in the form yyyy-MM-dd HH:mm:ss to convert to a Date object.
     * @return The Date object or null in case of a parsing error.
     */
    public static Date getDateFromDateTime(String dateString){
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the year from a given date.
     * @param date The Date object to get the year from.
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
}
