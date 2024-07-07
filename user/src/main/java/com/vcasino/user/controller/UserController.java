package com.vcasino.user.controller;

import com.vcasino.user.dto.UserDto;
import com.vcasino.user.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
@Validated
@Slf4j
public class UserController {

    private final UserService userService;

    @PatchMapping("/deposit")
    public ResponseEntity<UserDto> deposit(@RequestParam BigDecimal value,
                                           @RequestHeader("loggedInUser") String username) {
        log.info("REST request to deposit");
        UserDto userDto = userService.deposit(value, username);
        return ResponseEntity.ok().body(userDto);
    }

//    @PatchMapping("/withdraw")
//    public ResponseEntity<UserDto> deposit(@RequestParam BigDecimal value) {
//        log.info("REST request to change balance");
//        UserDto userDto = userService.changeUserBalance(value);
//        return ResponseEntity.ok().body(userDto);
//    }

    @PatchMapping
    public ResponseEntity<UserDto> update(@RequestBody @Valid UserDto userDto, @RequestHeader("loggedInUser") String username) {
        log.info("REST request to update user");
        UserDto updatedUserDto = userService.updateUser(userDto, username);
        return ResponseEntity.ok().body(updatedUserDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> findById(@PathVariable Long id) {
        log.info("REST request to get User#{}", id);
        UserDto foundUserDto = userService.findById(id);
        return ResponseEntity.ok().body(foundUserDto);
    }

//    @GetMapping("/balance")
//    public ResponseEntity<Boolean> enoughMoney(@RequestParam BigDecimal value) {
//        log.info("REST request to get  user");
//    }

}
