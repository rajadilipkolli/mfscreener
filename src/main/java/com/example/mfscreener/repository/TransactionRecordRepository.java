package com.example.mfscreener.repository;

import com.example.mfscreener.entities.TransactionRecord;
import com.example.mfscreener.model.PortfolioDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, Long> {

    @Transactional(readOnly = true)
    @Query(nativeQuery = true,
            value = """
                    WITH
                            inView AS
                            (
                                    SELECT
                                            scheme_name ,
                                            folio_number,
                                            MAX(transaction_date) AS mDate
                                    FROM
                                            public.transaction_record
                                    GROUP BY
                                            scheme_name,
                                            folio_number
                            )
                    SELECT
                            t.scheme_name   AS schemaName ,
                            t.folio_number  AS folioNumber,
                            t.balance_units AS balanceUnits,
                            t.scheme_id     AS schemeId
                    FROM
                            transaction_record t,
                            inView
                    WHERE
                            t.scheme_name      = inView.scheme_name
                    AND     t.folio_number     = inView.folio_number
                    AND     t.transaction_date = inView.mDate
                    AND     t.balance_units    > 0
                    """)
    List<PortfolioDetails> getPortfolio();

    @Modifying
    @Transactional
    @Query(value = "update transaction_record set scheme_id =:schemeId where scheme_name =:schemaName", nativeQuery = true)
    int updateSchemeId(@Param("schemeId") Long schemeId, @Param("schemaName") String schemaName);
}