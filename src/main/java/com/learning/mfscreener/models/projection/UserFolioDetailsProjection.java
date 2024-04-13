package com.learning.mfscreener.models.projection;

/**
 * Projection for {@link com.learning.mfscreener.entities.UserFolioDetailsEntity}
 */
public record UserFolioDetailsProjection(String folio, Long id, String scheme, Long amfi) {}
