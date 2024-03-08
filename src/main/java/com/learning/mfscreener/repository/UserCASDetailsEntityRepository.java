package com.learning.mfscreener.repository;

import com.learning.mfscreener.entities.UserCASDetailsEntity;
import com.learning.mfscreener.models.projection.PortfolioDetailsProjection;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserCASDetailsEntityRepository extends JpaRepository<UserCASDetailsEntity, Long> {

    @Transactional(readOnly = true)
    @Query(
            nativeQuery = true,
            value =
                    """
                    WITH tempView AS (
                        SELECT
                            utd.balance,
                            COALESCE(mf.scheme_name, usd.scheme) AS schemeName,
                            usd.amfi AS schemeId,
                            ufd.folio AS folioNumber,
                            ROW_NUMBER() OVER (
                                PARTITION BY utd.user_scheme_detail_id
                                ORDER BY utd.transaction_date DESC,
                                CASE
                                    WHEN utd.type = 'REDEMPTION' THEN balance
                                    ELSE balance * -1 -- Negate balance for descending order happens when 2 entries on same date
                                END ASC -- Ascending order for redemption, descending otherwise
                            ) AS row_number
                        FROM
                            user_transaction_details utd
                            JOIN user_scheme_details usd ON utd.user_scheme_detail_id = usd.id
                            JOIN user_folio_details ufd ON usd.user_folio_id = ufd.id
                            LEFT JOIN mf_scheme mf ON usd.amfi = mf.scheme_id
                        WHERE
                            utd.type NOT IN ('STAMP_DUTY_TAX', '*** Stamp Duty ***', 'STT_TAX')
                            AND ufd.pan = :pan
                            AND utd.transaction_date <= :asOfDate
                    )
                    SELECT
                        SUM(balance) AS balanceUnits,
                        schemeName,
                        schemeId,
                        folioNumber
                    FROM
                        tempView
                    WHERE
                        row_number = 1
                        AND balance <> 0
                    GROUP BY
                        schemeName,
                        schemeId,
                        folioNumber
                    """)
    List<PortfolioDetailsProjection> getPortfolioDetails(
            @Param("pan") String panNumber, @Param("asOfDate") LocalDate asOfDate);

    @Transactional(readOnly = true)
    @Query(
            """
              select u from UserCASDetailsEntity u join fetch u.folioEntities join fetch u.investorInfoEntity as i
              where i.email = :email and i.name = :name
              """)
    UserCASDetailsEntity findByInvestorEmailAndName(@Param("email") String email, @Param("name") String name);
}
