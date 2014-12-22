package com.development.jaba.utilities;

import com.development.jaba.model.Car;
import com.development.jaba.moneypit.MoneyPitApp;

import java.text.DateFormat;
import java.util.Date;

/**
 * Helper class for converting values to a specific string.
 */
public class FormattingHelper {

    /**
     * Gets a {@link String} from the application resources using it's resource name.
     * @param resourceName The name (key) of the string resource to get.
     * @return The {@link String} with the value of the given resource or the resource name itself
     * if it was not found in the resources.
     */
    public static String getStringByResourceName(String resourceName) {
        String packageName = MoneyPitApp.getContext().getPackageName();
        int resId = MoneyPitApp.getContext().getResources().getIdentifier(resourceName, "string", packageName);
        if(resId != 0) {
            return MoneyPitApp.getContext().getResources().getString(resId);
        }
        return resourceName;
    }

    /**
     * Converts the given integer value to a time span in days string.
     * @param days The number of days to convert.
     * @return The {@link String} containing the time span in days.
     */
    public static String toSpanInDays(int days) {
        return String.format("(%d %s)", days, getStringByResourceName("days"));
    }

    /**
     * Converts a price value to a {@link String} using the currency indicator of the
     * given {@link Car}.
     * @param car The {@link Car} entity to use.
     * @param price The price value to convert.
     * @return The {@link String} containing the price value.
     */
    public static String toPrice(Car car, double price) {
        if(car == null) {
            return String.format("%.02f", price);
        }
        return String.format("%s %.2f", car.getCurrency(), price);
    }

    /**
     * Converts a price-per-volume-unit value to a {@link String} using the currency indicator and
     * {@link com.development.jaba.model.VolumeUnit} of the given {@link Car}.
     * @param car The {@link Car} entity to use.
     * @param price The price value to convert.
     * @return The {@link String} containing the price-per-volume-unit value.
     */
    public static String toPricePerVolumeUnit(Car car, double price) {
        if(car == null) {
            return String.format("%.02f", price);
        }
        return String.format("%s %.2f/%s", car.getCurrency(), price, car.getVolumeUnit().getShortUnitName());
    }

    /**
     * Converts a volume-unit value to a {@link String} using the
     * {@link com.development.jaba.model.VolumeUnit} of the given {@link Car}.
     * @param car The {@link Car} entity to use.
     * @param volume The volume value to convert.
     * @return The {@link String} containing the volume-unit value.
     */
    public static String toVolumeUnit(Car car, double volume) {
        if(car == null) {
            return String.format("%.02f", volume);
        }
        return String.format("%.2f%s", volume, car.getVolumeUnit().getShortUnitName());
    }

    /**
     * Converts a fuel economy value to a {@link String} using the {@link com.development.jaba.model.DistanceUnit} and
     * {@link com.development.jaba.model.VolumeUnit} of the given {@link Car}.
     * @param car The {@link Car} entity to use.
     * @param economy The fuel economy value to convert.
     * @return The {@link String} containing the fuel economy value.
     */
    public static String toEconomy(Car car, double economy) {
        if(car == null) {
            return String.format("%.02f", economy);
        }
        return String.format("%.2f%s/%s", economy, car.getDistanceUnit().getShortUnitName(), car.getVolumeUnit().getShortUnitName());
    }

    /**
     * Converts a distance value to a {@link String} using the {@link com.development.jaba.model.DistanceUnit}
     * of the given {@link Car}.
     * @param car The {@link Car} entity to use.
     * @param distance The distance value to convert.
     * @return The {@link String} containing the distance value.
     */
    public static String toDistance(Car car, double distance) {
        if(car == null) {
            return String.format("%.0f", distance);
        }
        return String.format("%.0f%s", distance, car.getDistanceUnit().getShortUnitName());
    }

    /**
     * Converts a Date value to a {@link String} using the system configured short date
     * format.
     * @param date The {@link Date} value to convert.
     * @return The {@link String} containing the short date value.
     */
    public static String toShortDate(Date date) {
        DateFormat sdf = android.text.format.DateFormat.getDateFormat(MoneyPitApp.getContext());
        return sdf.format(date);
    }
}
