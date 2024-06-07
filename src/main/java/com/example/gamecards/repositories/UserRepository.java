package com.example.gamecards.repositories;

import com.example.gamecards.models.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<User,Long>
{
    @Query("select u from User u where u.email = ?1")
    User findByEmail(String email);

    boolean existsByEmail(String email);


}
