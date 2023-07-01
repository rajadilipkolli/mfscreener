/* Licensed under Apache-2.0 2021-2022. */
package com.learning.mfscreener.exception;

import java.time.LocalDate;
import lombok.Getter;

public class NavNotFoundException extends RuntimeException {

    @Getter
    private final LocalDate date;

    public NavNotFoundException(String message, LocalDate date) {
        super(message + " on " + date.toString());
        this.date = date;
    }
}
