package com.oddball.challenges.user;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    List<User> findAllByIdIn(Collection<Long> id);

}

