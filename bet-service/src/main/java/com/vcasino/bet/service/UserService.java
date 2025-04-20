package com.vcasino.bet.service;

import com.vcasino.bet.entity.User;
import com.vcasino.bet.exception.AppException;
import com.vcasino.bet.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public void createUser(Long id) {
        userRepository.save(new User(id, false));
    }

    public User getById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new AppException("User#" + userId + " not found", HttpStatus.NOT_FOUND));
    }

}
