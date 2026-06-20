package com.wallet.safewallet.exception;

public class DailyLimitExceededException extends RuntimeException{
    public DailyLimitExceededException(String message) {
        super(message);
    }
}
