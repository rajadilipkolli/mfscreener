package com.learning.mfscreener.repository;

import com.learning.mfscreener.entities.UserTransactionDetailsEntity;
import com.learning.mfscreener.models.projection.UserTransactionDetailsProjection;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTransactionDetailsEntityRepository extends JpaRepository<UserTransactionDetailsEntity, Long> {

    @Query(
            """
            select u from UserTransactionDetailsEntity u
            where u.userSchemeDetailsEntity.id = :id and u.type not in ('STT_TAX', 'STAMP_DUTY_TAX', 'MISC') and u.transactionDate <= :asOfDate
            order by u.transactionDate
            """)
    List<UserTransactionDetailsProjection> getByUserSchemeIdAndTypeNotInAndTransactionDateLessThanEqual(
            @Param("id") Long id, @Param("asOfDate") LocalDate transactionDate);

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
