package com.development.jaba.database;

public enum VolumeUnit {
    Liter(1),
    ImperialGallon(2),
    USGallon(3),
    Unknown(-1);

    private int _value;

    VolumeUnit(int value) {
        _value = value;
    }

    public int getValue() {
        return _value;
    }

    public VolumeUnit fromValue(int value) {
        for (VolumeUnit vu: VolumeUnit.values()) {
            if (vu._value == value) {
                return vu;
            }
        }
        return Unknown;
    }
}
