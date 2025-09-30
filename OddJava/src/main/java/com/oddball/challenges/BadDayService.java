package com.oddball.challenges;

import com.oddball.challenges.mood.Mood;
import com.oddball.challenges.mood.MoodRepository;
import com.oddball.challenges.stress.Stress;
import com.oddball.challenges.stress.StressRepository;
import com.oddball.challenges.user.User;
import com.oddball.challenges.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

import static com.oddball.challenges.Utils.toDate;
import static com.oddball.challenges.Utils.toLocalDate;
import static java.util.Comparator.comparing;
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class BadDayService {
    private static final int MIN_BAD_DAYS_THRESHOLD = 2;

    private final MoodRepository moodRepository;
    private final StressRepository stressRepository;
    private final UserRepository userRepository;

    public List<BadWeek> getBadWeeks(LocalDate weekToCheck) {
        LocalDate startOfWeek = Utils.getStartOfWeek(weekToCheck);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        List<Stress> stressesForWeek = stressRepository.findAllByDateBetween(toDate(startOfWeek), toDate(endOfWeek));
        List<Mood> moodsForWeek = moodRepository.findAllByDateBetween(toDate(startOfWeek), toDate(endOfWeek));

        Map<Long, Set<LocalDate>> badDaysByUserId = new HashMap<>();

        for (Mood mood : moodsForWeek) {
            Stress associatedStress = stressesForWeek.stream()
                .filter(s -> s.getUserId() == mood.getUserId() && s.getDate().equals(mood.getDate()))
                .findFirst()
                .orElse(null);

            if (isBadMood(mood, associatedStress)) {
                badDaysByUserId.computeIfAbsent(mood.getUserId(), __ -> new HashSet<>())
                    .add(toLocalDate(mood.getDate()));
            }
        }

        for (Stress stress : stressesForWeek) {
            if (stress.isBadStress()) {
                badDaysByUserId.computeIfAbsent(stress.getUserId(), __ -> new HashSet<>())
                    .add(toLocalDate(stress.getDate()));
            }
        }

        Map<Long, User> usersById = userRepository.findAllByIdIn(badDaysByUserId.keySet())
            .stream()
            .collect(toMap(User::getId, identity()));

        return badDaysByUserId.entrySet()
            .stream()
            .map(e -> {
                User user = usersById.get(e.getKey());
                Set<LocalDate> badDaysForUser = e.getValue();
                return new BadWeek(user.getUserName(), badDaysForUser.size());
            })
            .filter(bw -> bw.numberOfBadDays() >= MIN_BAD_DAYS_THRESHOLD)
            .sorted(comparing(BadWeek::numberOfBadDays).reversed())
            .toList();
    }

    private boolean isBadMood(Mood mood, Stress associatedStress) {
        return switch (mood.getMood()) {
            // moods of 1 or 2 are always bad
            case 1, 2 -> true;

            // moods of 3 are bad only if associated stress is bad
            case 3 -> associatedStress != null && associatedStress.isBadStress();

            // other moods are not bad
            default -> false;
        };
    }
}
