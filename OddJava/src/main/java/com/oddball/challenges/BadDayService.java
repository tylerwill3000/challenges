package com.oddball.challenges;

import com.oddball.challenges.mood.MoodRepository;
import com.oddball.challenges.stress.StressRepository;
import com.oddball.challenges.user.User;
import com.oddball.challenges.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

import static com.oddball.challenges.Utils.*;
import static java.util.Comparator.comparing;
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class BadDayService {
    private static final int MIN_BAD_DAYS_THRESHOLD = 2;
    private static final int MAX_BAD_DAY_STREAKS = 5;

    private final MoodRepository moodRepository;
    private final StressRepository stressRepository;
    private final UserRepository userRepository;

    public List<BadWeek> getBadWeeks(LocalDate weekToCheck) {
        LocalDate startOfWeek = getStartOfWeek(weekToCheck);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        Map<Long, TreeSet<BadDayDto>> badDaysByUser = getBadDaysByUser(startOfWeek, endOfWeek);
        Map<Long, User> usersById = getUsersById(badDaysByUser.keySet());

        return badDaysByUser.entrySet()
            .stream()
            .map(e -> {
                User user = usersById.get(e.getKey());
                return new BadWeek(user.getUserName(), e.getValue().size());
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

        // order streaks by largest to smallest
        badDayStreaks.sort((a, b) -> Integer.compare(b.size(), a.size()));

        Map<Long, User> usersById = getUsersById(allBadDays.keySet());

        return badDayStreaks.subList(0, MAX_BAD_DAY_STREAKS)
            .stream()
            .map(streak -> {
                BadDayDto firstBadDay = streak.getFirst();
                User user = usersById.get(firstBadDay.userId());
                return new BadDayStreak(user.getUserName(), firstBadDay.date(), streak.size());
            })
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
                boolean isConsecutive = toLocalDate(badDay.date()).minusDays(1).equals(toLocalDate(previousBadDay.date()));
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

    private Map<Long, User> getUsersById(Set<Long> userIds) {
        return userRepository.findAllByIdIn(userIds)
            .stream()
            .collect(toMap(User::getId, identity()));
    }

    /**
     * Get all bad days (from mood and stress) grouped by user for a given date range inclusive
     * @param from Starting day (inclusive) to retrieve bad days for. Null = no lower bound
     * @param to Ending day (inclusive) to retrieve bad days for. Null = no upper bound
     * @return A mapping of user IDs to a sorted set of bad days (sorted by day) which fall within the specified date range
     */
    private Map<Long, TreeSet<BadDayDto>> getBadDaysByUser(LocalDate from, LocalDate to) {
        List<BadDayDto> badMoodDays = moodRepository.getBadMoodDays(from == null ? null : toDate(from), to == null ? null : toDate(to));
        List<BadDayDto> badStressDays = stressRepository.getBadStressDays(from == null ? null : toDate(from), to == null ? null : toDate(to));

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
