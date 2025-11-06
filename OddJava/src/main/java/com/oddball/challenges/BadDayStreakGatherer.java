package com.oddball.challenges;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

/**
 * A {@link Gatherer} that collects consecutive {@link BadDay}s into streaks and emits each one as a list.
 * This gatherer assumes that the input stream of {@link BadDay}s are sorted in chronological order.
 */
class BadDayStreakGatherer implements Gatherer<BadDay, List<BadDay>, List<BadDay>> {
    static final BadDayStreakGatherer INSTANCE = new BadDayStreakGatherer();

    private BadDayStreakGatherer() {}

    @Override
    public Supplier<List<BadDay>> initializer() {
        return ArrayList::new;
    }

    @Override
    public Gatherer.Integrator<List<BadDay>, BadDay, List<BadDay>> integrator() {
        return Integrator.ofGreedy((currentStreak, nextDay, downstream) -> {
            if (currentStreak.isEmpty()) {
                // initial streak
                currentStreak.add(nextDay);
                return true;
            }

            BadDay previousBadDay = currentStreak.getLast();
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
    public BiConsumer<List<BadDay>, Downstream<? super List<BadDay>>> finisher() {
        // emit the last streak if we have a running one
        return (currentStreak, downstream) -> {
            if (!currentStreak.isEmpty()) {
                downstream.push(List.copyOf(currentStreak));
            }
        };
    }
}