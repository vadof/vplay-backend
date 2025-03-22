package com.vcasino.wallet.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CurrencyConversionException extends AppException {
    public CurrencyConversionException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
