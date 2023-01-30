package com.example.fakebank.exception;

public class InvalidRequestAccountException extends IllegalArgumentException {
    public InvalidRequestAccountException() {
        super("Invalid request account exception");
    }
}
