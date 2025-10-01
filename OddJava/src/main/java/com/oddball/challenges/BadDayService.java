package com.oddball.challenges;

import com.oddball.challenges.mood.MoodRepository;
import com.oddball.challenges.stress.StressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

import static com.oddball.challenges.Utils.getStartOfWeek;
import static java.util.Comparator.comparing;

@Service
@RequiredArgsConstructor
public class BadDayService {
    private static final int MIN_BAD_DAYS_THRESHOLD = 2;
    private static final int MAX_BAD_DAY_STREAKS = 5;

    private final MoodRepository moodRepository;
    private final StressRepository stressRepository;

    public List<BadWeek> getBadWeeks(LocalDate weekToCheck) {
        LocalDate startOfWeek = getStartOfWeek(weekToCheck);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        Map<Long, TreeSet<BadDayDto>> badDaysByUser = getBadDaysByUser(startOfWeek, endOfWeek);

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
        List<List<BadDayDto>> badDayStreaks = new ArrayList<>();

        Map<Long, TreeSet<BadDayDto>> allBadDays = getBadDaysByUser(null, null);
        allBadDays.forEach((userId, badDays) -> {
            List<List<BadDayDto>> userStreaks = parseStreaks(badDays);
            badDayStreaks.addAll(userStreaks);
        });

        return badDayStreaks.stream()
            .map(streak -> {
                BadDayDto firstBadDay = streak.getFirst();
                return new BadDayStreak(firstBadDay.userName(), firstBadDay.date(), streak.size());
            })
            .sorted(comparing(BadDayStreak::streakLength).reversed())
            .limit(MAX_BAD_DAY_STREAKS)
            .toList();
    }

    public static List<List<BadDayDto>> parseStreaks(TreeSet<BadDayDto> badDays) {
        List<List<BadDayDto>> streaks = new ArrayList<>();

        List<BadDayDto> currentStreak = new ArrayList<>();
        BadDayDto previousBadDay = null;
        for (BadDayDto badDay : badDays) {
            if (currentStreak.isEmpty()) {
                // initial streak
                currentStreak.add(badDay);
            } else {
                // continuing (potentially) an existing streak
                boolean isConsecutive = badDay.date().minusDays(1).equals(previousBadDay.date());
                if (isConsecutive) {
                    // streak continues
                    currentStreak.add(badDay);
                } else {
                    // streak is broken, save the current streak and start a new one
                    streaks.add(List.copyOf(currentStreak));
                    currentStreak.clear();
                    currentStreak.add(badDay);
                }
            }

            previousBadDay = badDay;
        }

        // process final bad streak (if we have leftovers)
        if (!currentStreak.isEmpty()) {
            streaks.add(currentStreak);
        }

        return streaks;
    }

    /**
     * Get all bad days (from mood and stress) grouped by user for a given date range inclusive
     * @param from Starting day (inclusive) to retrieve bad days for. Null = no lower bound
     * @param to Ending day (inclusive) to retrieve bad days for. Null = no upper bound
     * @return A mapping of user IDs to a sorted set of bad days (sorted by day) which fall within the specified date range
     */
    private Map<Long, TreeSet<BadDayDto>> getBadDaysByUser(LocalDate from, LocalDate to) {
        List<BadDayDto> badMoodDays = moodRepository.getBadMoodDays(from, to);
        List<BadDayDto> badStressDays = stressRepository.getBadStressDays(from, to);

        Set<BadDayDto> allBadDays = new HashSet<>();
        allBadDays.addAll(badMoodDays);
        allBadDays.addAll(badStressDays);

        Map<Long, TreeSet<BadDayDto>> badDaysByUser = new HashMap<>();
        for (BadDayDto badDay : allBadDays) {
            badDaysByUser.computeIfAbsent(badDay.userId(), __ -> new TreeSet<>(comparing(BadDayDto::date)))
                .add(badDay);
        }
        return badDaysByUser;
    }
}
