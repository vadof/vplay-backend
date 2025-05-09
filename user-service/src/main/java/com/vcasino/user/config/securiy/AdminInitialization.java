package com.vcasino.user.config.securiy;

import com.vcasino.user.config.ApplicationConfig;
import com.vcasino.user.dto.UserDto;
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
    private final ApplicationConfig applicationConfig;

    @Bean
    void initAdmin() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            log.info("Create admin");
            UserDto admin = UserDto.builder()
                    .username("admin")
                    .email("admin@vcasino.com")
                    .password(applicationConfig.getAdminPassword())
                    .name("Super Admin")
                    .build();
            authenticationService.registerAdmin(admin, null);
        } else {
            log.info("Admin exists, no need to create");
        }
    }

}
