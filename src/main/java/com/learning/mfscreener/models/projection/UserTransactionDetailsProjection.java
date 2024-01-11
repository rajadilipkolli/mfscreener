package com.learning.mfscreener.models.projection;

import java.time.LocalDate;

/**
 * Projection for {@link com.learning.mfscreener.entities.UserTransactionDetailsEntity}
 */
public interface UserTransactionDetailsProjection {
    LocalDate getTransactionDate();

    Double getAmount();
}
