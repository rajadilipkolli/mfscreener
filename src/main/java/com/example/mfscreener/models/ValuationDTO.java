/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.models;

import java.io.Serializable;

public record ValuationDTO(String date, String nav, String value) implements Serializable {}
