package com.development.jaba.model;

import com.development.jaba.utilities.DateHelper;

import java.util.HashMap;
import java.util.List;

/**
 * Summary information for a car. All values are computed from a
 * fixed range of fill up information.
 */
public class CarSummary {
    public double AverageFuelEconomy;
    public double AverageFuelCostPerVolumeUnit;
    public double AverageFuelCostPerDistanceUnit;
    public double TotalFuelCost;
    public double TotalDistance;
    public double TotalVolume;
    public double AverageCostPerMonth;
    public DatedValue MostExpensiveFillup;
    public DatedValue LeastExpensiveFillup;
    public DatedValue BestEconomyFillup;
    public DatedValue WorstEconomyFillup;
    public DatedValue MostExpensiveMonth;
    public DatedValue LeastExpensiveMonth;

    /**
     * Compute the summary information for the given fill-up set.
     *
     * @param data The {@link java.util.List} containing the {@link com.development.jaba.model.Fillup} entities
     *             to summarize.
     */
    public void setup(List<Fillup> data) {

        // Start of by resetting everything.
        TotalDistance = 0;
        TotalFuelCost = 0;
        TotalVolume = 0;

        BestEconomyFillup = new DatedValue();
        WorstEconomyFillup = new DatedValue();
        MostExpensiveFillup = new DatedValue();
        LeastExpensiveFillup = new DatedValue();
        MostExpensiveMonth = new DatedValue();
        LeastExpensiveMonth = new DatedValue();

        AverageFuelEconomy = 0;
        AverageFuelCostPerVolumeUnit = 0;
        AverageFuelCostPerDistanceUnit = 0;
        AverageCostPerMonth = 0;

        HashMap<String, Double> sum = new HashMap<>();

        double maxEcon = 0, minEcon = Double.MAX_VALUE, economy;
        double maxPrice = 0, minPrice = Double.MAX_VALUE, price;
        double totEcon = 0;
        int numTotEcon = 0;

        // Iterate the fill-ups and summarize the information.
        for (Fillup f : data) {

            // Totals for distance, price and volume.
            TotalDistance += f.getDistance();
            TotalFuelCost += f.getTotalPrice();
            TotalVolume += f.getVolume();

            // Determine the best and worst fuel economy. Fuel economy is only
            // calculated over the full fill-ups.
            economy = f.getFuelConsumption();
            if (economy > 0 && f.getFullTank()) {
                if (economy > maxEcon) {
                    BestEconomyFillup.Value = economy;
                    BestEconomyFillup.Date = f.getDate();
                    maxEcon = economy;
                }

                if (economy < minEcon) {
                    WorstEconomyFillup.Value = economy;
                    WorstEconomyFillup.Date = f.getDate();
                    minEcon = economy;
                }
                totEcon += economy;
                numTotEcon++;
            }

            // Determine the most expensive and the least expensive fill-ups.
            price = f.getTotalPrice();
            if (price > 0) {
                if (price > maxPrice) {
                    MostExpensiveFillup.Value = price;
                    MostExpensiveFillup.Date = f.getDate();
                    maxPrice = price;
                }

                if (price < minPrice) {
                    LeastExpensiveFillup.Value = price;
                    LeastExpensiveFillup.Date = f.getDate();
                    minPrice = price;
                }

                // Summarize the cost per month.
                String d = DateHelper.toMonthYearKey(f.getDate());
                double value = 0;
                if (sum.containsKey(d)) {
                    value = sum.get(d);
                }
                value += f.getTotalPrice();
                sum.put(d, value);
            }
        }

        // Determine the most expensive month and the least expensive month.
        double maxPMon = 0, minPMon = Double.MAX_VALUE, pmon;
        double total = 0;

        for (String key : sum.keySet()) {
            pmon = sum.get(key);
            if (pmon > maxPMon) {
                MostExpensiveMonth.Value = pmon;
                MostExpensiveMonth.Date = DateHelper.fromMonthYearKey(key);
                maxPMon = pmon;
            }

            if (pmon < minPMon) {
                LeastExpensiveMonth.Value = pmon;
                LeastExpensiveMonth.Date = DateHelper.fromMonthYearKey(key);
                minPMon = pmon;
            }
            total += pmon;
        }

        // Compute the averages.
        AverageCostPerMonth = total / sum.size();
        AverageFuelEconomy = totEcon / numTotEcon;
        AverageFuelCostPerVolumeUnit = TotalFuelCost / TotalVolume;
        AverageFuelCostPerDistanceUnit = TotalFuelCost / TotalDistance;
    }
}