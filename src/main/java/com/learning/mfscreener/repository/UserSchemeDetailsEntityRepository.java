package com.learning.mfscreener.repository;

import com.learning.mfscreener.entities.UserSchemeDetailsEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserSchemeDetailsEntityRepository extends JpaRepository<UserSchemeDetailsEntity, Long> {

    @Transactional(readOnly = true)
    List<UserSchemeDetailsEntity> findByAmfiIsNull();

    @Transactional
    @Modifying
    @Query("update UserSchemeDetailsEntity u set u.amfi = ?1, u.isin = ?2 where u.id = ?3")
    int updateAmfiAndIsinById(Long amfi, String isin, Long id);
}
