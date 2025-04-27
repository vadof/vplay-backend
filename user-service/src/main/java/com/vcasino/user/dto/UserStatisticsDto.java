package com.vcasino.user.dto;

import com.vcasino.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserStatisticsDto {
    UserRepository.UserGeneralStatistics generalStatistics;
    List<UserRepository.UserRegistrationStatistics> registrationStatistics;
}
