package com.learning.mfscreener.models.projection;

import java.util.List;

/**
 * Projection for {@link com.learning.mfscreener.entities.UserFolioDetailsEntity}
 */
public interface UserFolioDetailsProjection {
    String getFolio();

    List<UserSchemeDetailsEntityInfo> getSchemeEntities();

    /**
     * Projection for {@link com.learning.mfscreener.entities.UserSchemeDetailsEntity}
     */
    interface UserSchemeDetailsEntityInfo {
        Long getId();

        String getScheme();

        Long getAmfi();
    }
}
