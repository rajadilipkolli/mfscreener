/* Licensed under Apache-2.0 2023. */
package com.example.mfscreener.exception;

import org.apache.commons.math3.exception.TooManyEvaluationsException;

public class XIRRException extends Throwable {
    public XIRRException(String message, TooManyEvaluationsException e) {
        super("Unable to calculate XIRR: " + message, e);
    }
}
