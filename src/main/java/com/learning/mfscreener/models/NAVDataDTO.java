/* Licensed under Apache-2.0 2021-2022. */
package com.learning.mfscreener.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.time.LocalDate;

public record NAVDataDTO(
        @JsonFormat(pattern = "dd-MM-yyyy", shape = JsonFormat.Shape.STRING) LocalDate date, Double nav, Long schemeId)
        implements Serializable {
    public NAVDataDTO setSchemeId(Long schemeCode) {
        return new NAVDataDTO(date(), nav(), schemeCode);
    }
}
