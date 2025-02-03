package com.vcasino.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {

    @Size(max = 100, message = "Name must be no more than 100 characters long")
    String name;

    @Size(min = 2, max = 16, message = "Username must be between 2 and 16 characters")
    @NotEmpty(message = "Field cannot be empty")
    String username;

    @NotEmpty(message = "Field cannot be empty")
    @Email(message = "Invalid email")
    String email;

    @Size(min = 8, message = "Min password length is 8 symbols")
    @NotEmpty(message = "Field cannot be empty")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String password;
}
