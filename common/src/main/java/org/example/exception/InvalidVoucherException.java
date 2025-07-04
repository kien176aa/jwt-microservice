package org.example.exception;

public class InvalidVoucherException extends RuntimeException {
    public InvalidVoucherException(String message) {
        super(message);
    }
}
