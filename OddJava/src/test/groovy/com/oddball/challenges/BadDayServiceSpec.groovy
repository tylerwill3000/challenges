package com.oddball.challenges

import com.oddball.challenges.mood.Mood
import com.oddball.challenges.stress.Stress
import com.oddball.challenges.stress.StressRepository
import com.oddball.challenges.user.User
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification

import javax.sql.DataSource
import java.time.LocalDate

@SpringBootTest
class BadDayServiceSpec extends Specification {
    @Autowired
    BadDayService badDayService

    @Autowired
    DataSource dataSource

    @Autowired
    EntityManager entityManager

    def setup() {
        // clear existing test data out from import.sql since we'll manage this ourselves in the test
        dataSource.connection.withCloseable {
            it.createStatement().executeUpdate('DELETE FROM users')
            it.createStatement().executeUpdate('DELETE FROM moods')
            it.createStatement().executeUpdate('DELETE FROM stress')
        }
    }

    @Transactional
    def 'get bad weeks returns correct data'() {
        given: 'A week to query bad days for'
            LocalDate startOfWeek = LocalDate.of(2025, 9, 28) // a Sunday

            LocalDate sunday = startOfWeek
            LocalDate monday = startOfWeek.plusDays(1)
            LocalDate tuesday = startOfWeek.plusDays(2)
            LocalDate wednesday = startOfWeek.plusDays(3)

        and: 'A user with a good day and a lot of bad days'
            User userSomeBadDays = new User(name: 'someBadDays', userName: 'someBadDays_username', zipCode: '22222')
            entityManager.persist(userSomeBadDays)

            // sunday (good mood and good stress - GOOD DAY)
            entityManager.persist(new Mood(userId: userSomeBadDays.id, mood: 4, date: sunday))
            entityManager.persist(new Stress(userId: userSomeBadDays.id, stress: 3, date: sunday))

            // monday (bad mood, good stress - BAD DAY)
            entityManager.persist(new Mood(userId: userSomeBadDays.id, mood: 2, date: monday))
            entityManager.persist(new Stress(userId: userSomeBadDays.id, stress: 2, date: monday))

            // tuesday (neutral mood, but bad stress - BAD DAY)
            entityManager.persist(new Mood(userId: userSomeBadDays.id, mood: 3, date: tuesday))
            entityManager.persist(new Stress(userId: userSomeBadDays.id, stress: 4, date: tuesday))

            // wednesday (no mood, but bad stress - BAD DAY)
            entityManager.persist(new Stress(userId: userSomeBadDays.id, stress: 4, date: wednesday))

        and: 'A user with only bad days, but fewer bad days than the previous'
            User userAllBadDays = new User(name: 'allBadDays', userName: 'allBadDays_username', zipCode: '3333')
            entityManager.persist(userAllBadDays)

            // sunday (bad mood and good stress - BAD DAY)
            entityManager.persist(new Mood(userId: userAllBadDays.id, mood: 2, date: sunday))
            entityManager.persist(new Stress(userId: userAllBadDays.id, stress: 1, date: sunday))

            // monday (bad mood, bad stress - BAD DAY)
            entityManager.persist(new Mood(userId: userAllBadDays.id, mood: 1, date: monday))
            entityManager.persist(new Stress(userId: userAllBadDays.id, stress: 4, date: monday))

        and: 'A user with no bad days'
            User userNoBadDays = new User(name: 'noBadDays', userName: 'noBadDays_username', zipCode: '55124')
            entityManager.persist(userNoBadDays)

            // good mood, good stress for all 7 days
            (0..6).each { dayOffset ->
                entityManager.persist(new Mood(userId: userNoBadDays.id, date: startOfWeek.plusDays(dayOffset), mood: 4))
                entityManager.persist(new Stress(userId: userNoBadDays.id, date: startOfWeek, stress: 1))
            }

        when: 'we ask for bad weeks'
            List<BadWeek> badWeeks = badDayService.getBadWeeks(startOfWeek)

        then: 'we have 2 entries for the 2 users with bad days'
            badWeeks.size() == 2

        and: 'the first entry is for the user with the most bad days'
            BadWeek mostBadDays = badWeeks[0]
            verifyAll(mostBadDays) {
                userName() == userSomeBadDays.userName
                numberOfBadDays() == 3
            }

        and: 'the second entry is for the user with only bad days'
            BadWeek secondMostBadDays = badWeeks[1]
            verifyAll(secondMostBadDays) {
                userName() == userAllBadDays.userName
                numberOfBadDays() == 2
            }
    }

    @Transactional
    def 'get bad day streaks returns correct data'() {
        given: 'An arbitrary day to start creating moods and stresses for'
            LocalDate startOfWeek = LocalDate.of(2025, 9, 28) // a Sunday

            LocalDate sunday = startOfWeek
            LocalDate monday = startOfWeek.plusDays(1)
            LocalDate tuesday = startOfWeek.plusDays(2)
            LocalDate wednesday = startOfWeek.plusDays(3)
            LocalDate thursday = startOfWeek.plusDays(4)
            LocalDate friday = startOfWeek.plusDays(5)
            LocalDate saturday = startOfWeek.plusDays(6)

        and: 'A user with a streak of 2 bad days, a good day, then 3 bad days'
            User userWithStreaks = new User(name: 'streaks', userName: 'streaks_username', zipCode: '44444')
            entityManager.persist(userWithStreaks)

            // sunday (bad mood, good stress - BAD DAY)
            entityManager.persist(new Mood(userId: userWithStreaks.id, mood: 2, date: sunday))
            entityManager.persist(new Stress(userId: userWithStreaks.id, stress: 2, date: sunday))

            // monday (neutral mood, but bad stress - BAD DAY)
            entityManager.persist(new Mood(userId: userWithStreaks.id, mood: 3, date: monday))
            entityManager.persist(new Stress(userId: userWithStreaks.id, stress: 4, date: monday))

            // tuesday (good mood and good stress - GOOD DAY)
            entityManager.persist(new Mood(userId: userWithStreaks.id, mood: 4, date: tuesday))
            entityManager.persist(new Stress(userId: userWithStreaks.id, stress: 1, date: tuesday))

            // wednesday (no mood, but bad stress - BAD DAY)
            entityManager.persist(new Stress(userId: userWithStreaks.id, stress: 4, date: wednesday))

            // thursday (bad mood and bad stress - BAD DAY)
            entityManager.persist(new Mood(userId: userWithStreaks.id, mood: 1, date: thursday))
            entityManager.persist(new Stress(userId: userWithStreaks.id, stress: 4, date: thursday))

            // friday (bad mood and neutral stress - BAD DAY)
            entityManager.persist(new Mood(userId: userWithStreaks.id, mood: 2, date: friday))
            entityManager.persist(new Stress(userId: userWithStreaks.id, stress: 3, date: friday))

            // saturday (good mood and good stress - GOOD DAY)
            entityManager.persist(new Mood(userId: userWithStreaks.id, mood: 4, date: saturday))
            entityManager.persist(new Stress(userId: userWithStreaks.id, stress: 1, date: saturday))

        when: 'we ask for bad day streaks'
            List<BadDayStreak> badDayStreaks = badDayService.getBadDayStreaks()

        then: 'we have 2 entries for the 2 bad day streaks, ordered by their length'
            badDayStreaks.size() == 2

            BadDayStreak longestStreak = badDayStreaks[0]
            verifyAll(longestStreak) {
                userName() == userWithStreaks.userName
                it.streakLength() == 3
                startDate() == wednesday
            }

            BadDayStreak shorterStreak = badDayStreaks[1]
            verifyAll(shorterStreak) {
                userName() == userWithStreaks.userName
                streakLength() == 2
                startDate() == sunday
            }
    }
}
