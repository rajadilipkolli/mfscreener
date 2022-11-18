/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.repository;

import com.example.mfscreener.entities.UserCASDetailsEntity;
import com.example.mfscreener.models.projection.PortfolioDetailsProjection;
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
                     select
              sum(tempView.balance) as balanceUnits,
              usd.scheme as schemeName,
              usd.amfi as schemeId
            from
              (
                select
                  balance,
                  user_scheme_detail_id,
                  row_number() over (
                    partition by user_scheme_detail_id
                    order by
                      transaction_date desc
                  ) as row_number
                from
                  user_transaction_details
                where
                  type NOT IN ('STAMP_DUTY_TAX', 'STT_TAX')
              ) tempView
              join user_scheme_details usd on tempView.user_scheme_detail_id = usd.id
              join user_folio_details ufd on usd.user_folio_id = ufd.id
            where
              row_number = 1
              and balance <> 0
              and ufd.pan = :pan
            group by
              usd.scheme,
              usd.amfi
                    """)
    List<PortfolioDetailsProjection> getPortfolioDetails(@Param("pan") String panNumber);
}
