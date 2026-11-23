package com.learning.mfscreener.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.learning.mfscreener.adapter.ConversionServiceAdapter;
import com.learning.mfscreener.entities.UserCASDetailsEntity;
import com.learning.mfscreener.entities.UserFolioDetailsEntity;
import com.learning.mfscreener.entities.UserSchemeDetailsEntity;
import com.learning.mfscreener.entities.UserTransactionDetailsEntity;
import com.learning.mfscreener.mapper.CasDetailsMapper;
import com.learning.mfscreener.models.portfolio.CasDTO;
import com.learning.mfscreener.models.portfolio.InvestorInfoDTO;
import com.learning.mfscreener.models.portfolio.UserFolioDTO;
import com.learning.mfscreener.models.portfolio.UserSchemeDTO;
import com.learning.mfscreener.models.portfolio.UserTransactionDTO;
import com.learning.mfscreener.models.response.UploadResponseHolder;
import com.learning.mfscreener.utils.UserCasEntityGraphBuilder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PortfolioServiceDeltaTest {
    private static final String EMAIL = "investor@example.com";
    private static final String NAME = "Investor";

    private final CasDetailsMapper mapper = mock(CasDetailsMapper.class);
    private final UserTransactionDetailsService transactions = mock(UserTransactionDetailsService.class);
    private final UserSchemeDetailsService schemes = mock(UserSchemeDetailsService.class);
    private final PortfolioServiceHelper helper = mock(PortfolioServiceHelper.class);
    private final UserCASDetailsService casDetails = mock(UserCASDetailsService.class);
    private final InvestorInfoService investors = mock(InvestorInfoService.class);
    private PortfolioService portfolio;

    @BeforeEach
    void setUp() {
        portfolio = new PortfolioService(
                mock(ConversionServiceAdapter.class),
                mapper,
                transactions,
                mock(SchemeService.class),
                schemes,
                helper,
                casDetails,
                investors,
                mock(CapitalGainsService.class));
    }

    @Test
    void rebuildsNestedAssociationsWithoutDuplicatingFetchedRows() {
        UserCASDetailsEntity cas = new UserCASDetailsEntity().setId(1L);
        UserFolioDetailsEntity folio = new UserFolioDetailsEntity().setId(2L).setFolio("existing");
        cas.setFolioEntities(new ArrayList<>(List.of(folio, folio)));
        UserSchemeDetailsEntity scheme = new UserSchemeDetailsEntity().setId(3L).setIsin("ISIN");
        scheme.setUserFolioDetailsEntity(new UserFolioDetailsEntity().setId(2L));
        UserTransactionDetailsEntity transaction = new UserTransactionDetailsEntity().setId(4L);
        transaction.setUserSchemeDetailsEntity(new UserSchemeDetailsEntity().setId(3L));

        UserCasEntityGraphBuilder.rebuild(cas, List.of(scheme, scheme), List.of(transaction, transaction));

        assertThat(cas.getFolioEntities()).containsExactly(folio);
        assertThat(folio.getUserCasDetailsEntity()).isSameAs(cas);
        assertThat(folio.getSchemeEntities()).containsExactly(scheme);
        assertThat(scheme.getUserFolioDetailsEntity()).isSameAs(folio);
        assertThat(scheme.getTransactionEntities()).containsExactly(transaction);
        assertThat(transaction.getUserSchemeDetailsEntity()).isSameAs(scheme);
    }

    @Test
    void repeatImportKeepsExistingGraphAndNewFolio() {
        UserCASDetailsEntity cas = new UserCASDetailsEntity().setId(1L);
        UserFolioDetailsEntity existing = new UserFolioDetailsEntity().setId(2L).setFolio("existing");
        cas.setFolioEntities(new ArrayList<>(List.of(existing)));
        UserSchemeDetailsEntity scheme = new UserSchemeDetailsEntity().setId(3L).setIsin("ISIN");
        scheme.setUserFolioDetailsEntity(existing);
        UserTransactionDetailsEntity transaction = new UserTransactionDetailsEntity().setId(4L);
        transaction.setUserSchemeDetailsEntity(scheme);
        UserFolioDetailsEntity added = new UserFolioDetailsEntity().setFolio("added");
        UserSchemeDetailsEntity addedScheme = new UserSchemeDetailsEntity().setIsin("NEW");
        added.addSchemeEntity(addedScheme);
        UserTransactionDetailsEntity addedTransaction = new UserTransactionDetailsEntity();
        addedScheme.addTransactionEntity(addedTransaction);

        UserFolioDTO oldRequest = mock(UserFolioDTO.class);
        when(oldRequest.folio()).thenReturn("existing");
        UserFolioDTO newRequest = mock(UserFolioDTO.class);
        when(newRequest.folio()).thenReturn("added");
        UserSchemeDTO newScheme = mock(UserSchemeDTO.class);
        when(newScheme.transactions()).thenReturn(List.of(mock(UserTransactionDTO.class)));
        when(newRequest.schemes()).thenReturn(List.of(newScheme));
        CasDTO request = mock(CasDTO.class);
        when(request.folios()).thenReturn(List.of(oldRequest, newRequest));
        when(helper.countTransactionsByUserFolioDTOList(request.folios())).thenReturn(2L);
        when(casDetails.findByInvestorEmailAndName(EMAIL, NAME)).thenReturn(cas);
        when(schemes.getSchemesByEmailAndName(EMAIL, NAME)).thenReturn(List.of(scheme));
        when(transactions.findAllTransactionsByEmailAndName(EMAIL, NAME)).thenReturn(List.of(transaction));
        when(mapper.mapUserFolioDTOToUserFolioDetailsEntity(newRequest)).thenReturn(added);

        UploadResponseHolder result = portfolio.findDelta(EMAIL, NAME, request);

        assertThat(result.folioCount()).isEqualTo(1);
        assertThat(result.transactionsCount()).isEqualTo(1);
        assertThat(result.userCASDetailsEntity().getFolioEntities()).containsExactly(existing, added);
        assertThat(existing.getSchemeEntities()).containsExactly(scheme);
        assertThat(scheme.getTransactionEntities()).containsExactly(transaction);
        assertThat(added.getSchemeEntities()).containsExactly(addedScheme);
        assertThat(addedScheme.getTransactionEntities()).containsExactly(addedTransaction);
    }

    @Test
    void savesMixedImportOnceAndMatchesTransactionsWithinEachFolio() {
        LocalDate oldDate = LocalDate.of(2020, 1, 1);
        LocalDate newDate = LocalDate.of(2021, 1, 1);
        LocalDate addedFolioDate = LocalDate.of(2022, 1, 1);
        UserCASDetailsEntity cas = new UserCASDetailsEntity().setId(1L);
        UserFolioDetailsEntity folio = new UserFolioDetailsEntity().setId(2L).setFolio("existing");
        UserFolioDetailsEntity otherFolio =
                new UserFolioDetailsEntity().setId(5L).setFolio("other");
        cas.addFolioEntity(folio);
        cas.addFolioEntity(otherFolio);

        UserSchemeDetailsEntity scheme = new UserSchemeDetailsEntity().setId(3L).setIsin("ISIN");
        UserSchemeDetailsEntity otherScheme =
                new UserSchemeDetailsEntity().setId(6L).setIsin("ISIN");
        folio.addSchemeEntity(scheme);
        otherFolio.addSchemeEntity(otherScheme);
        UserTransactionDetailsEntity oldTransaction =
                new UserTransactionDetailsEntity().setId(4L).setTransactionDate(oldDate);
        UserTransactionDetailsEntity otherTransaction =
                new UserTransactionDetailsEntity().setId(7L).setTransactionDate(newDate);
        scheme.addTransactionEntity(oldTransaction);
        otherScheme.addTransactionEntity(otherTransaction);

        UserFolioDetailsEntity addedFolio = new UserFolioDetailsEntity().setFolio("added");
        UserSchemeDetailsEntity addedScheme = new UserSchemeDetailsEntity().setIsin("ISIN");
        UserTransactionDetailsEntity addedTransaction =
                new UserTransactionDetailsEntity().setTransactionDate(addedFolioDate);
        addedFolio.addSchemeEntity(addedScheme);
        addedScheme.addTransactionEntity(addedTransaction);

        UserTransactionDTO newRequestTransaction = requestTransaction(newDate);
        UserFolioDTO requestFolio = requestFolio("existing", requestTransaction(oldDate), newRequestTransaction);
        UserFolioDTO otherRequestFolio = requestFolio("other", requestTransaction(newDate));
        UserFolioDTO addedRequestFolio = requestFolio("added", requestTransaction(addedFolioDate));
        CasDTO request = new CasDTO(
                null,
                null,
                null,
                new InvestorInfoDTO(EMAIL, NAME, null, null),
                List.of(requestFolio, otherRequestFolio, addedRequestFolio));
        UserTransactionDetailsEntity newTransaction = new UserTransactionDetailsEntity().setTransactionDate(newDate);

        when(investors.existsByEmailAndName(EMAIL, NAME)).thenReturn(true);
        when(helper.countTransactionsByUserFolioDTOList(request.folios())).thenReturn(4L);
        when(casDetails.findByInvestorEmailAndName(EMAIL, NAME)).thenReturn(cas);
        when(casDetails.saveEntity(cas)).thenReturn(cas);
        when(schemes.getSchemesByEmailAndName(EMAIL, NAME)).thenReturn(List.of(scheme, otherScheme));
        when(transactions.findAllTransactionsByEmailAndName(EMAIL, NAME))
                .thenReturn(List.of(oldTransaction, otherTransaction));
        when(mapper.mapUserFolioDTOToUserFolioDetailsEntity(addedRequestFolio)).thenReturn(addedFolio);
        when(mapper.transactionDTOToTransactionEntity(newRequestTransaction)).thenReturn(newTransaction);

        assertThat(portfolio.processCasDTO(request)).isEqualTo("Imported 1 folios and 2 transactions");

        verify(casDetails).saveEntity(cas);
        verify(schemes, never()).saveUserScheme(any());
        assertThat(cas.getFolioEntities()).containsExactly(folio, otherFolio, addedFolio);
        assertThat(scheme.getTransactionEntities()).containsExactly(oldTransaction, newTransaction);
        assertThat(newTransaction.getUserSchemeDetailsEntity()).isSameAs(scheme);
        assertThat(otherScheme.getTransactionEntities()).containsExactly(otherTransaction);
        assertThat(addedFolio.getSchemeEntities()).containsExactly(addedScheme);
        assertThat(addedScheme.getTransactionEntities()).containsExactly(addedTransaction);
    }

    private UserFolioDTO requestFolio(String folioNumber, UserTransactionDTO... transactions) {
        UserSchemeDTO scheme = mock(UserSchemeDTO.class);
        when(scheme.isin()).thenReturn("ISIN");
        when(scheme.transactions()).thenReturn(List.of(transactions));
        return new UserFolioDTO(folioNumber, null, null, null, null, List.of(scheme));
    }

    private UserTransactionDTO requestTransaction(LocalDate date) {
        UserTransactionDTO transaction = mock(UserTransactionDTO.class);
        when(transaction.date()).thenReturn(date);
        return transaction;
    }

    @Test
    void existingInvestorWithNoTransactionsCanImportNewFolio() {
        UserCASDetailsEntity cas = new UserCASDetailsEntity().setId(1L);
        UserFolioDetailsEntity existing = new UserFolioDetailsEntity().setId(2L).setFolio("existing");
        cas.setFolioEntities(new ArrayList<>(List.of(existing)));
        UserSchemeDetailsEntity emptyScheme =
                new UserSchemeDetailsEntity().setId(3L).setIsin("EMPTY");
        emptyScheme.setUserFolioDetailsEntity(existing);
        UserFolioDetailsEntity added = new UserFolioDetailsEntity().setFolio("added");

        UserFolioDTO oldRequest = mock(UserFolioDTO.class);
        when(oldRequest.folio()).thenReturn("existing");
        UserFolioDTO newRequest = mock(UserFolioDTO.class);
        when(newRequest.folio()).thenReturn("added");
        UserSchemeDTO newScheme = mock(UserSchemeDTO.class);
        when(newScheme.transactions()).thenReturn(List.of(mock(UserTransactionDTO.class)));
        when(newRequest.schemes()).thenReturn(List.of(newScheme));
        CasDTO request = mock(CasDTO.class);
        when(request.folios()).thenReturn(List.of(oldRequest, newRequest));
        when(helper.countTransactionsByUserFolioDTOList(request.folios())).thenReturn(1L);
        when(casDetails.findByInvestorEmailAndName(EMAIL, NAME)).thenReturn(cas);
        when(schemes.getSchemesByEmailAndName(EMAIL, NAME)).thenReturn(List.of(emptyScheme));
        when(transactions.findAllTransactionsByEmailAndName(EMAIL, NAME)).thenReturn(List.of());
        when(mapper.mapUserFolioDTOToUserFolioDetailsEntity(newRequest)).thenReturn(added);

        UploadResponseHolder result = portfolio.findDelta(EMAIL, NAME, request);

        assertThat(result.folioCount()).isEqualTo(1);
        assertThat(result.userCASDetailsEntity().getFolioEntities()).containsExactly(existing, added);
        assertThat(existing.getSchemeEntities()).containsExactly(emptyScheme);
        assertThat(emptyScheme.getTransactionEntities()).isEmpty();
    }
}
