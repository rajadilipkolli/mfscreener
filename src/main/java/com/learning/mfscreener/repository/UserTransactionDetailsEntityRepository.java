package com.learning.mfscreener.repository;

import com.learning.mfscreener.entities.UserTransactionDetailsEntity;
import com.learning.mfscreener.models.projection.UserTransactionDetailsProjection;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTransactionDetailsEntityRepository extends JpaRepository<UserTransactionDetailsEntity, Long> {
    List<UserTransactionDetailsProjection> findByUserSchemeDetailsEntity_IdAndTypeNotIn(
            Long id, Collection<String> types);
}
