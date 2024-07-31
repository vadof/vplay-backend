package com.vcasino.user.service;

import com.vcasino.user.dto.CountryDto;
import com.vcasino.user.mapper.CountryMapper;
import com.vcasino.user.repository.CountryRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class CountryService {

    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;

    public List<CountryDto> getCountries() {
        return countryMapper.toDtos(countryRepository.findAll());
    }

}
