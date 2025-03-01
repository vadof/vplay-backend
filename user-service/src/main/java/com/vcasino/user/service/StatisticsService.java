package com.vcasino.user.service;

import com.vcasino.user.exception.AppException;
import com.vcasino.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class StatisticsService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserRepository.UserStatistics getStatistics() {
        return userRepository.fetchUserStatistics();
    }

    @Transactional(readOnly = true)
    public UserRepository.UserInformation getUserInformation(Long id, String username) {
        UserRepository.UserInformation res;
        if (id != null) {
            res = userRepository.fetchUserInformation(id);
        } else {
            res = userRepository.fetchUserInformation(username);
        }

        if (res == null) {
            throw new AppException("User not found", HttpStatus.NOT_FOUND);
        }

        return res;
    }
}
