package com.development.jaba.model;

import java.util.Date;

/**
 * Class representing a value and a date. Used for the
 * {@link com.development.jaba.model.CarSummary}.
 */
public class DatedValue {
    public DatedValue() {
        Date = new Date();
    }

    public Date Date;
    public double Value;
}