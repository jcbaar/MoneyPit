package com.development.jaba.model;

/**
 * Enumeration for volume units.
 */
public enum VolumeUnit {
    Liter(1),
    ImperialGallon(2),
    USGallon(3);

    private int _value;

    /**
     * Constructor. Initializes an instance of the object.
     * @param value The integer value of the instance.
     */
    private VolumeUnit(int value) {
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
