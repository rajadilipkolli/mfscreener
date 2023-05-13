/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.repository;

import com.example.mfscreener.entities.UserSchemeDetailsEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSchemeDetailsEntityRepository extends JpaRepository<UserSchemeDetailsEntity, Long> {

    @Query("select distinct(amfi) u from UserSchemeDetailsEntity u")
    List<Long> findDistinctByAmfi();
}
