package com.vcasino.bet.repository;

import com.vcasino.bet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
