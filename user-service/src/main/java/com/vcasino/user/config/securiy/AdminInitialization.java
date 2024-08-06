package com.vcasino.user.config.securiy;

import com.vcasino.user.dto.CountryDto;
import com.vcasino.user.dto.UserDto;
import com.vcasino.user.entity.Role;
import com.vcasino.user.repository.UserRepository;
import com.vcasino.user.service.AuthenticationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class AdminInitialization {

    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;

    @Bean
    void initAdmin() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            log.info("Create admin");
            UserDto admin = UserDto.builder()
                    .username("admin")
                    .email("admin@vcasino.com")
                    .password("R84+sc5+6'cx'(35qcmaf.=1;D9Hiq[j-J=ol$b{n)VaQ[HQ6N")
                    .firstname("Super")
                    .lastname("Admin")
                    .country(new CountryDto("EST", "Estonia"))
                    .build();
            authenticationService.register(admin, Role.ADMIN);
        } else {
            log.info("Admin exists, no need to create");
        }
    }

}
