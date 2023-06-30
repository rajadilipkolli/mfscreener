/* Licensed under Apache-2.0 2022. */
package com.learning.mfscreener.models.portfolio;

import java.io.Serializable;

public record ValuationDTO(String date, String nav, String value) implements Serializable {}
