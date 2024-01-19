package com.learning.mfscreener.repository;

import com.learning.mfscreener.entities.UserTransactionDetailsEntity;
import com.learning.mfscreener.models.projection.UserTransactionDetailsProjection;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserTransactionDetailsEntityRepository extends JpaRepository<UserTransactionDetailsEntity, Long> {

    List<UserTransactionDetailsProjection> findByUserSchemeDetailsEntity_IdAndTypeNotInOrderByTransactionDateAsc(
            Long id, Collection<String> types);

    @Transactional(readOnly = true)
    @Query(
            """
            select utd from UserTransactionDetailsEntity utd
                inner join fetch utd.userSchemeDetailsEntity usd
                inner join fetch usd.userFolioDetailsEntity ufd
                inner join fetch ufd.userCasDetailsEntity ucd
                inner join fetch ucd.investorInfoEntity ii
            where ii.email = :email and ii.name = :name
            """)
    List<UserTransactionDetailsEntity> findAllTransactionsByEmailAndName(
            @Param("email") String email, @Param("name") String name);
}
