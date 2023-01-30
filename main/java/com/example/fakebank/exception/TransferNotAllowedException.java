package com.example.fakebank.exception;

public class TransferNotAllowedException extends RuntimeException {
    public TransferNotAllowedException() {
        super("Transfer Not Allowed exception");
    }
}
