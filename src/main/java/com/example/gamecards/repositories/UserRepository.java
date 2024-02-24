package com.example.gamecards.repositories;

import com.example.gamecards.models.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User,Long> 
{
    @Query("select u from User u where u.email = ?1")
    User findByEmail(String email);


}
