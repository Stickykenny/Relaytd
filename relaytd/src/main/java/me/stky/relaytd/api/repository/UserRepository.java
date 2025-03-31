package me.stky.relaytd.api.repository;

import me.stky.relaytd.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
        User findByUsername(String username);
    }
