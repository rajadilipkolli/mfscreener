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

    @Query(
            """
              select u from UserCASDetailsEntity u join fetch u.folioEntities join fetch u.investorInfoEntity as i
              where i.email = :email and i.name = :name
              """)
    UserCASDetailsEntity findByInvestorEmailAndName(@Param("email") String email, @Param("name") String name);
}
