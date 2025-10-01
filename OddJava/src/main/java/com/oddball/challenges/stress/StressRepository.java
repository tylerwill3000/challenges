package com.oddball.challenges.stress;

import com.oddball.challenges.BadDayDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface StressRepository extends CrudRepository<Stress, Long> {
    @Query("""
        select new com.oddball.challenges.BadDayDto(s.userId, s.date)
        from Stress s
        left join Mood m on m.userId=s.userId and m.date=s.date
        where (s.stress in (4, 5) and coalesce(m.mood, 3)=3)
        and (:startDate is null or m.date >= :startDate)
        and (:endDate is null or m.date <= :endDate)
    """)
    List<BadDayDto> getBadStressDays(@Param("startDate") Date startDate,
                                     @Param("endDate") Date endDate);

}

