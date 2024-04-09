/* Licensed under Apache-2.0 2021-2024. */
package com.learning.mfscreener.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.time.LocalDate;

public record NAVDataDTO(
        @JsonFormat(pattern = "dd-MM-yyyy", shape = JsonFormat.Shape.STRING) LocalDate date, Float nav, Long schemeId)
        implements Serializable {

    public NAVDataDTO withSchemeId(Long schemeCode) {
        return new NAVDataDTO(date(), nav(), schemeCode);
    }
}
