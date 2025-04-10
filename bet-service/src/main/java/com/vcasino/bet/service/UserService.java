package com.vcasino.bet.service;

import com.vcasino.bet.entity.User;
import com.vcasino.bet.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public void createUser(Long id) {
        userRepository.save(new User(id, false));
    }

}
