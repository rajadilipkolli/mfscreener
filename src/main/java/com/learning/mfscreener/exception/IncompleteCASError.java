package com.learning.mfscreener.exception;

public class IncompleteCASError extends Throwable {
    private final String message;

    public IncompleteCASError(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
