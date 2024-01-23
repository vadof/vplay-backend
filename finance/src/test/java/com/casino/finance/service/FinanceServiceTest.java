package com.casino.finance.service;

import com.casino.finance.dto.ConversionDto;
import com.casino.finance.dto.ExchangeRateDto;
import com.casino.finance.exceptions.AppException;
import com.casino.finance.util.Constants;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureMockMvc
public class FinanceServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FinanceService financeService;

    ConversionDto mockConversionDto() {
        return new ConversionDto("EUR", "USD", new BigDecimal("100.00"), null);
    }

    Map<String, Double> mockConversionRates() {
        Map<String, Double> map = new HashMap<>();
        map.put("EUR", 1.00d);
        map.put("GBP", 1.16d);
        map.put("USD", 0.91d);
        map.put("RUB", 96.7d);
        return map;
    }

    ExchangeRateDto mockExchangeRateDto(String baseCode) {
        return ExchangeRateDto.builder()
                .baseCode(baseCode)
                .conversionRates(mockConversionRates())
                .build();
    }

    @Test
    @DisplayName("Convert Money - Success")
    void convertMoneySuccess() {
        ConversionDto conversionDto = mockConversionDto();

        ExchangeRateDto exchangeRateDto = mockExchangeRateDto(conversionDto.getBaseCode());
        Mockito.when(restTemplate.getForObject(Mockito.any(String.class), Mockito.eq(ExchangeRateDto.class)))
                .thenReturn(exchangeRateDto);

        BigDecimal convertedPercent = new BigDecimal(String.valueOf(Constants.CONVERSION_INTEREST_PERCENT / 100d));
        BigDecimal commission = conversionDto.getAmount().multiply(convertedPercent);

        BigDecimal resultValue = conversionDto.getAmount()
                .subtract(commission)
                .multiply(new BigDecimal(String.valueOf(exchangeRateDto.getConversionRates().get("USD"))))
                .setScale(Constants.SCALE, Constants.ROUNDING_MODE);

        ConversionDto result = financeService.convertMoney(conversionDto);
        Assertions.assertThat(resultValue).isEqualTo(result.getResult());
    }

    @Test
    @DisplayName("Convert Money Invalid baseCode Failure")
    void convertMoneyInvalidBaseCodeFailure() {
        ConversionDto conversionDto = mockConversionDto();
        conversionDto.setBaseCode("CODE");

        Mockito.when(restTemplate.getForObject(Mockito.any(String.class), Mockito.eq(ExchangeRateDto.class)))
                .thenThrow(HttpClientErrorException.class);

        AppException exception = assertThrows(AppException.class, () -> financeService.convertMoney(conversionDto));

        Assertions.assertThat(exception.getMessage()).contains("baseCode");
        Assertions.assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Convert Money Invalid toBaseCode Failure")
    void convertMoneyInvalidToBaseCodeFailure() {
        ConversionDto conversionDto = mockConversionDto();
        conversionDto.setToBaseCode("CODE");

        Mockito.when(restTemplate.getForObject(Mockito.any(String.class), Mockito.eq(ExchangeRateDto.class)))
                .thenReturn(mockExchangeRateDto(conversionDto.getBaseCode()));

        AppException exception = assertThrows(AppException.class, () -> financeService.convertMoney(conversionDto));

        Assertions.assertThat(exception.getMessage()).contains("toBaseCode");
        Assertions.assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

}
