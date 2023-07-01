/* Licensed under Apache-2.0 2021-2022. */
package com.learning.mfscreener.models;

import java.io.Serializable;

public record NAVDataDTO(String date, Double nav, Long schemeId) implements Serializable {
    public NAVDataDTO setSchemeId(Long schemeCode) {
        return new NAVDataDTO(date(), nav(), schemeCode);
    }
}
