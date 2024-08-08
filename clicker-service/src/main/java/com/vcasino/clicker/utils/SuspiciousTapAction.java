package com.vcasino.clicker.utils;

import com.vcasino.clicker.dto.Tap;
import com.vcasino.clicker.entity.Account;
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
public class SuspiciousTapAction {
    Account account;
    Tap tap;
    String message;
}
