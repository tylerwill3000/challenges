package com.oddball.challenges.stress;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface StressRepository extends CrudRepository<Stress, Long> {

    List<Stress> findAllByDateBetween(Date startDate, Date endDate);

}

