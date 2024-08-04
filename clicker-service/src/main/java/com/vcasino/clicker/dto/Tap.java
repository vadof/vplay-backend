package com.vcasino.clicker.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Tap {
    Integer count;
    Long timestamp;
}
