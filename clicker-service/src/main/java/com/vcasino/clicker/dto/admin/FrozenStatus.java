package com.vcasino.clicker.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FrozenStatus {
    @NotNull(message = "Field cannot be null")
    private Long accountId;
    @NotNull(message = "Field cannot be null")
    private Boolean status;
}
