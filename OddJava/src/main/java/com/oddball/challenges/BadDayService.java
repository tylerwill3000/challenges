package com.oddball.challenges;

import com.oddball.challenges.mood.MoodRepository;
import com.oddball.challenges.stress.StressRepository;
import com.oddball.challenges.user.User;
import com.oddball.challenges.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.oddball.challenges.Utils.*;
import static java.util.Comparator.comparing;
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.*;

@Service
@RequiredArgsConstructor
public class BadDayService {
    private static final int MIN_BAD_DAYS_THRESHOLD = 2;

    private final MoodRepository moodRepository;
    private final StressRepository stressRepository;
    private final UserRepository userRepository;

    public List<BadWeek> getBadWeeks(LocalDate weekToCheck) {
        LocalDate startOfWeek = getStartOfWeek(weekToCheck);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        Set<BadDayDto> allBadDays = getBadDays(startOfWeek, endOfWeek);

        Map<Long, Set<LocalDate>> badDaysByUser = new HashMap<>();
        for (BadDayDto badDay : allBadDays) {
            badDaysByUser.computeIfAbsent(badDay.userId(), __ -> new HashSet<>()).add(toLocalDate(badDay.date()));
        }

        Map<Long, User> usersById = userRepository.findAllByIdIn(badDaysByUser.keySet())
            .stream()
            .collect(toMap(User::getId, identity()));

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

    private Set<BadDayDto> getBadDays(LocalDate from, LocalDate to) {
        List<BadDayDto> badMoodDays = moodRepository.getBadMoodDays(from == null ? null : toDate(from), to == null ? null : toDate(to));
        List<BadDayDto> badStressDays = stressRepository.getBadStressDays(from == null ? null : toDate(from), to == null ? null : toDate(to));

        Set<BadDayDto> allBadDays = new HashSet<>(badMoodDays);
        allBadDays.addAll(badStressDays);
        return allBadDays;
    }
}
