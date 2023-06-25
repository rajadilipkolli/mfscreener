/* Licensed under Apache-2.0 2021-2022. */
package com.learning.mfscreener.models;

import java.io.Serializable;

public record NAVDataDTO(String date, String nav) implements Serializable {}
