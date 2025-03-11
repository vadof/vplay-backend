package com.vcasino.clickerdata.dto;

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
public class ChartData<X, Y> {
    List<X> labels;
    List<Y> data;
    List<ChartOption> options;
    ChartOption selectedOption;
}
