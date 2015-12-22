package com.development.jaba.model;

import com.development.jaba.utilities.FormattingHelper;

/**
 * Enumeration for distance units.
 */
public enum DistanceUnit
{
    Kilometer(1),
    Mile(2);

    private final int _value;

    /**
     * Constructor. Initializes an instance of the object.
     * @param value The integer value of the instance.
     */
    DistanceUnit(int value) {
        _value = value;
    }

    /**
     * Get's the integer value of the enum.
     * @return The integer value of the enum.
     */
    public int getValue() {
        return _value;
    }

    /**
     * Convert the unit to it's name.
     * @return The name of the unit.
     */
    public String getUnitName() {
        return FormattingHelper.getStringByResourceName("long" + this.toString());
    }

    /**
     * Convert the unit to it's short name.
     * @return The name of the unit.
     */
    public String getShortUnitName() {
        return FormattingHelper.getStringByResourceName("short" + this.toString());
    }

    /**
     * Converts an integer value to it's enum equivalent.
     * @param value The value to convert to DistanceUnit.
     * @return The DistanceUnit equivalency of the input value.
     * @throws IllegalArgumentException Thrown when the integer value is out of range.
     */
    public static DistanceUnit fromValue(int value) throws IllegalArgumentException {
        for (DistanceUnit du: DistanceUnit.values()) {
            if (du._value == value) {
                return du;
            }
        }
        throw new IllegalArgumentException();
    }
}
