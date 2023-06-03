package com.example.mfscreener.repository;

import com.example.mfscreener.entities.UserCASDetailsEntity;
import com.example.mfscreener.entities.UserTransactionDetailsEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserTransactionDetailsEntityRepository extends JpaRepository<UserTransactionDetailsEntity, Long> {

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

    List<UserTransactionDetailsEntity> findByUserSchemeDetailsEntity_UserFolioDetailsEntity_UserCasDetailsEntity(
            UserCASDetailsEntity userCasDetailsEntity);
}
