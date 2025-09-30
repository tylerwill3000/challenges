package com.oddball.challenges.mood;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface MoodRepository extends CrudRepository<Mood, Long> {

    List<Mood> findAllByDateBetween(Date startDate, Date endDate);

}

