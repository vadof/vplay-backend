package com.vcasino.finance.service;

import com.vcasino.finance.dto.ConversionDto;
import com.vcasino.finance.dto.ExchangeRateDto;
import com.vcasino.finance.exceptions.AppException;
import com.vcasino.finance.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinanceService {

    private final RestTemplate restTemplate;

    @Value("${exchangerate.api.key}")
    private String API_KEY;

    // TODO set min value to convert money and calculate scales correctly
    public ConversionDto convertMoney(ConversionDto conversionDto) {
        ConversionDto result = conversionDto.copy();

        ExchangeRateDto exchangeRate;
        try {
            exchangeRate = restTemplate.getForObject(
                    getUrl(result.getBaseCode()), ExchangeRateDto.class);
        } catch (Exception e) {
            throw new AppException(String.format("%s – Unsupported baseCode",
                    result.getBaseCode()), HttpStatus.BAD_REQUEST);
        }

        if (!exchangeRate.getConversionRates().containsKey(conversionDto.getToBaseCode())) {
            throw new AppException(String.format("%s – Unsupported toBaseCode",
                    conversionDto.getToBaseCode()), HttpStatus.BAD_REQUEST);
        }

        Double rate = exchangeRate.getConversionRates().get(result.getToBaseCode());

        BigDecimal commission = calculateServiceCommission(conversionDto.getAmount());
        result.setResult(calculateNewValue(result.getAmount(), commission, rate));

        lockInProfitFromCommission(conversionDto.getBaseCode(), conversionDto.getToBaseCode(), commission);

        return result;
    }

    private String getUrl(String baseCode) {
        return String.format("https://v6.exchangerate-api.com/v6/%s/latest/%s", API_KEY, baseCode);
    }

    private BigDecimal calculateNewValue(BigDecimal oldValue, BigDecimal commission, Double rate) {
        BigDecimal valueAfterCommission = oldValue.subtract(commission);
        return valueAfterCommission.multiply(new BigDecimal(String.valueOf(rate)))
                .setScale(Constants.SCALE, Constants.ROUNDING_MODE);
    }

    private BigDecimal calculateServiceCommission(BigDecimal value) {
        BigDecimal convertedPercent = new BigDecimal(String.valueOf(Constants.CONVERSION_INTEREST_PERCENT / 100d));
        return value.multiply(convertedPercent);
    }

    // TODO
    private void lockInProfitFromCommission(String from, String to, BigDecimal commission) {
        log.info("Conversion profit from {} to {} is {} {}", from, to, commission, from);
    }

}
