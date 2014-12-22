package com.development.jaba.model;

import com.development.jaba.utilities.FormattingHelper;

/**
 * Enumeration for volume units.
 */
public enum VolumeUnit {
    Liter(1),
    ImperialGallon(2),
    USGallon(3);

    private final int _value;

    /**
     * Constructor. Initializes an instance of the object.
     * @param value The integer value of the instance.
     */
    private VolumeUnit(int value) {
        _value = value;
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
     * Get's the integer value of the enum.
     * @return The integer value of the enum.
     */
    public int getValue() {
        return _value;
    }

    /**
     * Converts an integer value to it's enum equivalent.
     * @param value The value to convert to VolumeUnit.
     * @return The VolumeUnit equivalency of the input value.
     * @throws IllegalArgumentException Thrown when the integer value is out of range.
     */
    public static VolumeUnit fromValue(int value) throws IllegalArgumentException {
        for (VolumeUnit vu: VolumeUnit.values()) {
            if (vu._value == value) {
                return vu;
            }
        }
        throw new IllegalArgumentException();
    }
}
