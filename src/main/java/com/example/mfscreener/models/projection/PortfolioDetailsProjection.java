/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.models.projection;

public interface PortfolioDetailsProjection {

    String getSchemaName();

    String getFolioNumber();

    Float getBalanceUnits();

    Long getSchemeId();
}
