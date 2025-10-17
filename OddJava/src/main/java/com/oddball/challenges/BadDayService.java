package com.oddball.challenges;

import com.oddball.challenges.mood.MoodRepository;
import com.oddball.challenges.stress.StressRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;

@Service
public record BadDayService(MoodRepository moodRepository,
                            StressRepository stressRepository) {
    private static final int MIN_BAD_DAYS_THRESHOLD = 2;
    private static final int MAX_BAD_DAY_STREAKS = 5;

    public List<BadWeek> getBadWeeks(LocalDate weekToCheck) {
        LocalDate startOfWeek = getStartOfWeek(weekToCheck);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        Map<Long, TreeSet<BadDay>> badDaysByUser = getBadDaysByUser(startOfWeek, endOfWeek);

        return badDaysByUser.values()
            .stream()
            .map(userBadDays -> {
                // each batch of bad days belong to a single user, so we can just grab the first one to get the username
                String userName = userBadDays.getFirst().userName();
                return new BadWeek(userName, userBadDays.size());
            })
            .filter(bw -> bw.numberOfBadDays() >= MIN_BAD_DAYS_THRESHOLD)
            .sorted(comparing(BadWeek::numberOfBadDays).reversed())
            .toList();
    }

    public List<BadDayStreak> getBadDayStreaks() {
        Map<Long, TreeSet<BadDay>> allBadDays = getBadDaysByUser(null, null);

        List<List<BadDay>> allBadDayStreaks = new ArrayList<>();
        for (TreeSet<BadDay> userBadDays : allBadDays.values()) {
            List<List<BadDay>> userStreaks = userBadDays.stream()
                .gather(BadDayStreakGatherer.INSTANCE)
//                .peek(streak ->
//                    System.out.println("Found streak:\n" + streak.stream().map(o -> "    " + o).collect(joining("\n"))))
                .toList();
            allBadDayStreaks.addAll(userStreaks);
        }

        return allBadDayStreaks.stream()
            .sorted((a, b) -> Integer.compare(b.size(), a.size())) // largest streaks to smallest
            .limit(MAX_BAD_DAY_STREAKS)
            .map(streak -> {
                BadDay firstBadDay = streak.getFirst();
                return new BadDayStreak(firstBadDay.userName(), firstBadDay.date(), streak.size());
            })
            .toList();
    }

    /**
     * Get all bad days (from mood and stress) grouped by user for a given date range inclusive
     * @param from Starting day (inclusive) to retrieve bad days for. Null = no lower bound
     * @param to Ending day (inclusive) to retrieve bad days for. Null = no upper bound
     * @return A mapping of user IDs to a sorted set of bad days (sorted by day) which fall within the specified date range
     */
    private Map<Long, TreeSet<BadDay>> getBadDaysByUser(LocalDate from, LocalDate to) {
        List<BadDay> badMoodDays = moodRepository.getBadMoodDays(from, to);
        List<BadDay> badStressDays = stressRepository.getBadStressDays(from, to);

        var allBadDays = Stream.concat(badMoodDays.stream(), badStressDays.stream());
        return allBadDays.collect(
            groupingBy(BadDay::userId, toCollection(() -> new TreeSet<>(comparing(BadDay::date)))));
    }

    private static LocalDate getStartOfWeek(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return date;
        }

        return date.minusDays(date.getDayOfWeek().getValue());
    }
}
