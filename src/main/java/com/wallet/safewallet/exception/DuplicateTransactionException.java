package com.wallet.safewallet.exception;

public class DuplicateTransactionException extends RuntimeException{
    public DuplicateTransactionException(String message) {
        super(message);
    }
}
