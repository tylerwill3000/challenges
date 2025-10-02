package com.oddball.challenges;

import java.time.LocalDate;

public record BadDayDto(long userId,
                        String userName,
                        LocalDate date) implements Comparable<BadDayDto> {
    @Override
    public int compareTo(BadDayDto other) {
        return date.compareTo(other.date());
    }
}
