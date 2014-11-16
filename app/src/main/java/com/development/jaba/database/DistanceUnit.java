package com.development.jaba.database;

/**
 * Created by Jan on 25-10-2014.
 */
public enum DistanceUnit
{
    Kilometer(1),
    Mile(2),
    Unknown(-1);

    private int _value;

    DistanceUnit(int value) {
        _value = value;
    }

    public int getValue() {
        return _value;
    }

    public DistanceUnit fromValue(int value) {
        for (DistanceUnit du: DistanceUnit.values()) {
            if (du._value == value) {
                return du;
            }
        }
        return Unknown;
    }
}
