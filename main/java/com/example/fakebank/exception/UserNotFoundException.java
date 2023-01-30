package com.example.fakebank.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("User not found exception");
    }
}
