package com.oddball.challenges;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/bad-day")
@RequiredArgsConstructor
public class BadDayController {
    private final BadDayService badDayService;

    @GetMapping("/weeks/{weekToCheck}")
    public List<BadWeek> getBadWeeks(@PathVariable LocalDate weekToCheck) {
        return badDayService.getBadWeeks(weekToCheck);
    }

    @GetMapping("/streaks")
    public List<BadDayStreak> getBadDayStreaks() {
        return badDayService.getBadDayStreaks();
    }
}
