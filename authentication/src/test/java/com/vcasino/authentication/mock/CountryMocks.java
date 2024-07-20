package com.vcasino.authentication.mock;

import com.vcasino.authentication.dto.CountryDto;
import com.vcasino.authentication.entity.Country;

public class CountryMocks {
    public static Country getCountryMock() {
        return getCountryMock("EST", "Estonia");
    }

    public static Country getCountryMock(String code, String name) {
        return Country.builder()
                .code(code)
                .name(name)
                .build();
    }

    public static CountryDto getCountryDtoMock() {
        return getCountryDtoMock("EST", "Estonia");
    }

    public static CountryDto getCountryDtoMock(String code, String name) {
        return CountryDto.builder()
                .code(code)
                .name(name)
                .build();
    }
}
