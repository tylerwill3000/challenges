package com.oddball.challenges;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/bad-day")
public record BadDayController(BadDayService badDayService) {

    @GetMapping("/weeks/{weekToCheck}")
    public List<BadWeek> getBadWeeks(@PathVariable LocalDate weekToCheck) {
        return badDayService.getBadWeeks(weekToCheck);
    }

    @GetMapping("/streaks")
    public List<BadDayStreak> getBadDayStreaks() {
        return badDayService.getBadDayStreaks();
    }
}
