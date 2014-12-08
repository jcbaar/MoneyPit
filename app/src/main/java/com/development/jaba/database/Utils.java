package com.development.jaba.database;

import com.development.jaba.model.Fillup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Date;
import java.util.Locale;

/**
 * Static class containing some utility methods.
 */
public class Utils {
    /**
     * Recomputes the following fields of this given list of fillups:
     *
     * TotalPrice
     * DaysSinceLastFillup
     * Distance
     * FuelConsumption
     *
     * @param fillups The list of fillups that needs to be recomputed.
     * @param previous A fillup entity which is the first that precedes the earliest
     *                 fillup in the list. This is used to correctly compute the DaysSinceLastFillup,
     *                 Distance and FuelConsumption fields of the earliest fillup in the list.
     */
    public static void recomputeFillupTotals(List<Fillup> fillups, Fillup previous)
    {
        for (int i = 0; i < fillups.size(); i++)
        {
            Fillup fillup = fillups.get(i);
            fillup.setTotalPrice( fillup.getPrice() * fillup.getVolume());

            Fillup pfillup;
            if (i < fillups.size() - 1)
            {
                pfillup = fillups.get(i + 1);
            }
            else
            {
                pfillup = previous;
            }

            if (pfillup != null)
            {
                fillup.setDaysSinceLastFillup(getDateDiffInDays(pfillup.getDate(), fillup.getDate()));
                fillup.setDistance(fillup.getOdometer() - pfillup.getOdometer());
                fillup.setFuelConsumption(fillup.getDistance() / fillup.getVolume());
            }
        }
    }

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
     * Gets the difference in days between two given dates.
     * @param dateOne The first date (earlier)
     * @param dateTwo The second date (later)
     * @return The number of days between the two dates.
     */
    public static int getDateDiffInDays(Date dateOne, Date dateTwo)
    {
        long timeOne = dateOne.getTime();
        long timeTwo = dateTwo.getTime();
        long oneDay = 1000 * 60 * 60 * 24;
        long delta = (timeTwo - timeOne) / oneDay;
        return (int)delta;
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
