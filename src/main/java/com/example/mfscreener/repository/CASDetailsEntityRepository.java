/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.repository;

import com.example.mfscreener.entities.UserCASDetailsEntity;
import com.example.mfscreener.models.projection.PortfolioDetailsProjection;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CASDetailsEntityRepository extends JpaRepository<UserCASDetailsEntity, Long> {

    @Transactional(readOnly = true)
    @Query(
            nativeQuery = true,
            value =
                    """
                    with tempView as (
                        select
                          utd.balance,
                          utd.user_scheme_detail_id,
                          usd.scheme as schemeName,
                          usd.amfi as schemeId,
                          ufd.folio as folioNumber,
                          row_number() over (
                            partition by utd.user_scheme_detail_id
                            order by
                              utd.transaction_date desc
                          ) as row_number
                        from
                          user_transaction_details utd
                          join user_scheme_details usd on utd.user_scheme_detail_id = usd.id
                          join user_folio_details ufd on usd.user_folio_id = ufd.id
                        where
                          utd.type NOT IN ('STAMP_DUTY_TAX', 'STT_TAX')
                          and ufd.pan = :pan
                          and utd.transaction_date <= :asOfDate
                      )
                    select
                      sum(balance) as balanceUnits,
                      schemeName,
                      schemeId,
                      folioNumber
                      from tempView
                    where
                      row_number = 1
                      and balance <> 0
                    group by
                      schemeName,
                      schemeId,
                      folioNumber
                    """)
    List<PortfolioDetailsProjection> getPortfolioDetails(
            @Param("pan") String panNumber, @Param("asOfDate") LocalDate asOfDate);
}
