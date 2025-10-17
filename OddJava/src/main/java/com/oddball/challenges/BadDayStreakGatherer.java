package com.oddball.challenges;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

/**
 * A {@link Gatherer} that collects consecutive {@link BadDayDto}s into streaks and emits each one as a list.
 * This gatherer assumes that the input stream of {@link BadDayDto}s are sorted in chronological order.
 */
class BadDayStreakGatherer implements Gatherer<BadDayDto, List<BadDayDto>, List<BadDayDto>> {
    static final BadDayStreakGatherer INSTANCE = new BadDayStreakGatherer();

    private BadDayStreakGatherer() {}

    @Override
    public Supplier<List<BadDayDto>> initializer() {
        return ArrayList::new;
    }

    @Override
    public Gatherer.Integrator<List<BadDayDto>, BadDayDto, List<BadDayDto>> integrator() {
        return Integrator.ofGreedy((currentStreak, nextDay, downstream) -> {
            if (currentStreak.isEmpty()) {
                // initial streak
                currentStreak.add(nextDay);
                return true;
            }

            BadDayDto previousBadDay = currentStreak.getLast();
            boolean isConsecutive = nextDay.date().minusDays(1).equals(previousBadDay.date());
            if (isConsecutive) {
                // streak continues
                currentStreak.add(nextDay);
                return true;
            }

            // streak is broken - emit the current streak and start a new one
            downstream.push(List.copyOf(currentStreak));
            currentStreak.clear();
            currentStreak.add(nextDay);
            return true;
        });
    }

    @Override
    public BiConsumer<List<BadDayDto>, Downstream<? super List<BadDayDto>>> finisher() {
        // emit the last streak if we have a running one
        return (currentStreak, downstream) -> {
            if (!currentStreak.isEmpty()) {
                downstream.push(List.copyOf(currentStreak));
            }
        };
    }
}