package com.learning.mfscreener.exception;

public class GainsException extends Throwable {

    private final String message;

    public GainsException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
