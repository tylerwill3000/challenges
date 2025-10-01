package com.oddball.challenges;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class Utils {

    public static LocalDate getStartOfWeek(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return date;
        }

        return date.minusDays(date.getDayOfWeek().getValue());
    }
}
