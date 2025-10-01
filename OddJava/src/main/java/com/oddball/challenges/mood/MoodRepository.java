package com.oddball.challenges.mood;

import com.oddball.challenges.BadDayDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface MoodRepository extends CrudRepository<Mood, Long> {

    @Query("""
        select new com.oddball.challenges.BadDayDto(m.userId, m.date)
        from Mood m
        where m.mood in (1, 2)
        and (:startDate is null or m.date >= :startDate)
        and (:endDate is null or m.date <= :endDate)
    """)
    List<BadDayDto> getBadMoodDays(@Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

}

