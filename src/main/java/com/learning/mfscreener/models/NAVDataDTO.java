/* Licensed under Apache-2.0 2021-2024. */
package com.learning.mfscreener.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.With;

public record NAVDataDTO(
        @JsonFormat(pattern = "dd-MM-yyyy", shape = JsonFormat.Shape.STRING) LocalDate date,
        Float nav,
        @With Long schemeId)
        implements Serializable {}
