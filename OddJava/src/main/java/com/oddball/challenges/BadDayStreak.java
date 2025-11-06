package com.oddball.challenges;

import java.time.LocalDate;

public record BadDayStreak(String userName, LocalDate startDate, int streakLength) {
}
