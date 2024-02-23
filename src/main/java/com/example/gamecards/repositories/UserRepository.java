package com.example.gamecards.repositories;

import com.example.gamecards.models.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User,Long> {
}
