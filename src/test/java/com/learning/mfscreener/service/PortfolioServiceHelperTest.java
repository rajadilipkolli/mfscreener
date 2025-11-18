package com.learning.mfscreener.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.mfscreener.exception.NavNotFoundException;
import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.models.PortfolioDetailsDTO;
import com.learning.mfscreener.models.portfolio.UserFolioDTO;
import com.learning.mfscreener.models.portfolio.UserSchemeDTO;
import com.learning.mfscreener.models.portfolio.UserTransactionDTO;
import com.learning.mfscreener.models.projection.PortfolioDetailsProjection;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceHelperTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserCASDetailsService userCASDetailsService;

    @Mock
    private NavService navService;

    @Mock
    private XIRRCalculatorService xIRRCalculatorService;

    private PortfolioServiceHelper portfolioServiceHelper;

    @BeforeEach
    void setUp() {
        portfolioServiceHelper =
                new PortfolioServiceHelper(objectMapper, userCASDetailsService, navService, xIRRCalculatorService);
    }

    @Test
    @DisplayName("Should read value from bytes using ObjectMapper")
    void shouldReadValueFromBytesUsingObjectMapper() throws IOException {
        // Given
        byte[] bytes = "{\"name\":\"test\"}".getBytes();
        TestClass expectedObject = new TestClass("test");
        when(objectMapper.readValue(bytes, TestClass.class)).thenReturn(expectedObject);

        // When
        TestClass result = portfolioServiceHelper.readValue(bytes, TestClass.class);

        // Then
        assertThat(result).isEqualTo(expectedObject);
        verify(objectMapper).readValue(bytes, TestClass.class);
    }

    @Test
    @DisplayName("Should propagate IOException from ObjectMapper")
    void shouldPropagateIOExceptionFromObjectMapper() throws IOException {
        // Given
        byte[] bytes = "invalid json".getBytes();
        IOException expectedException = new IOException("Invalid JSON");
        when(objectMapper.readValue(bytes, TestClass.class)).thenThrow(expectedException);

        // When & Then
        assertThatThrownBy(() -> portfolioServiceHelper.readValue(bytes, TestClass.class))
                .isInstanceOf(IOException.class)
                .hasMessage("Invalid JSON");

        verify(objectMapper).readValue(bytes, TestClass.class);
    }

    @Test
    @DisplayName("Should join futures and return results")
    void shouldJoinFuturesAndReturnResults() {
        // Given
        List<CompletableFuture<String>> futures = new ArrayList<>();
        futures.add(CompletableFuture.completedFuture("first"));
        futures.add(CompletableFuture.completedFuture("second"));
        futures.add(CompletableFuture.completedFuture("third"));

        // When
        List<String> result = portfolioServiceHelper.joinFutures(futures);

        // Then
        assertThat(result).containsExactly("first", "second", "third");
    }

    @Test
    @DisplayName("Should handle empty futures list")
    void shouldHandleEmptyFuturesList() {
        // Given
        List<CompletableFuture<String>> futures = new ArrayList<>();

        // When
        List<String> result = portfolioServiceHelper.joinFutures(futures);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should count transactions across multiple folios and schemes")
    void shouldCountTransactionsAcrossMultipleFoliosAndSchemes() {
        // Given
        List<UserFolioDTO> folioDTOList = new ArrayList<>();

        // Create first folio with 2 schemes
        List<UserSchemeDTO> scheme1List = new ArrayList<>();
        scheme1List.add(createUserSchemeDTO(3)); // 3 transactions
        scheme1List.add(createUserSchemeDTO(2)); // 2 transactions

        UserFolioDTO folio1 = new UserFolioDTO("folio1", "AMC1", "PAN1", "OK", "OK", scheme1List);
        folioDTOList.add(folio1);

        // Create second folio with 1 scheme
        List<UserSchemeDTO> scheme2List = new ArrayList<>();
        scheme2List.add(createUserSchemeDTO(5)); // 5 transactions

        UserFolioDTO folio2 = new UserFolioDTO("folio2", "AMC2", "PAN2", "OK", "OK", scheme2List);
        folioDTOList.add(folio2);

        // When
        long result = portfolioServiceHelper.countTransactionsByUserFolioDTOList(folioDTOList);

        // Then
        assertThat(result).isEqualTo(10); // 3 + 2 + 5 = 10 transactions
    }

    @Test
    @DisplayName("Should return zero for empty folio list")
    void shouldReturnZeroForEmptyFolioList() {
        // Given
        List<UserFolioDTO> folioDTOList = new ArrayList<>();

        // When
        long result = portfolioServiceHelper.countTransactionsByUserFolioDTOList(folioDTOList);

        // Then
        assertThat(result).isEqualTo(0);
    }

    @Test
    @DisplayName("Should return zero when folios have no schemes")
    void shouldReturnZeroWhenFoliosHaveNoSchemes() {
        // Given
        List<UserFolioDTO> folioDTOList = new ArrayList<>();
        UserFolioDTO folioWithNoSchemes = new UserFolioDTO("folio1", "AMC1", "PAN1", "OK", "OK", new ArrayList<>());
        folioDTOList.add(folioWithNoSchemes);

        // When
        long result = portfolioServiceHelper.countTransactionsByUserFolioDTOList(folioDTOList);

        // Then
        assertThat(result).isEqualTo(0);
    }

    @Test
    @DisplayName("Should return zero when schemes have no transactions")
    void shouldReturnZeroWhenSchemesHaveNoTransactions() {
        // Given
        List<UserFolioDTO> folioDTOList = new ArrayList<>();
        List<UserSchemeDTO> schemeList = new ArrayList<>();
        schemeList.add(createUserSchemeDTO(0)); // No transactions

        UserFolioDTO folio = new UserFolioDTO("folio1", "AMC1", "PAN1", "OK", "OK", schemeList);
        folioDTOList.add(folio);

        // When
        long result = portfolioServiceHelper.countTransactionsByUserFolioDTOList(folioDTOList);

        // Then
        assertThat(result).isEqualTo(0);
    }

    @Test
    @DisplayName("Should get portfolio details by PAN and date with successful NAV lookup")
    void shouldGetPortfolioDetailsByPANAndDateWithSuccessfulNavLookup() throws NavNotFoundException {
        // Given
        String panNumber = "ABCDE1234F";
        LocalDate asOfDate = LocalDate.of(2023, 6, 15);

        PortfolioDetailsProjection projection = mock(PortfolioDetailsProjection.class);
        when(projection.getSchemeId()).thenReturn(123456L);
        when(projection.getSchemeName()).thenReturn("Test Scheme");
        when(projection.getFolioNumber()).thenReturn("TEST123");
        when(projection.getBalanceUnits()).thenReturn(100.0);
        when(projection.getSchemeDetailId()).thenReturn(789L);

        List<PortfolioDetailsProjection> projections = List.of(projection);
        when(userCASDetailsService.getPortfolioDetailsByPanAndAsOfDate(panNumber, asOfDate))
                .thenReturn(projections);

        MFSchemeDTO schemeDTO =
                new MFSchemeDTO("Test AMC", 123456L, "GROWTH", "Test Scheme", "50.25", "15-Jun-2023", "EQUITY");
        when(navService.getNavByDateWithRetry(123456L, asOfDate)).thenReturn(schemeDTO);

        when(xIRRCalculatorService.calculateXIRRBySchemeId(123456L, 789L, asOfDate))
                .thenReturn(0.15);

        // When
        List<PortfolioDetailsDTO> result =
                portfolioServiceHelper.getPortfolioDetailsByPANAndAsOfDate(panNumber, asOfDate);

        // Then
        assertThat(result).hasSize(1);
        PortfolioDetailsDTO portfolioDetails = result.get(0);
        assertThat(portfolioDetails.totalValue()).isEqualTo(5025.0); // 100 * 50.25
        assertThat(portfolioDetails.schemeName()).isEqualTo("Test Scheme");
        assertThat(portfolioDetails.folioNumber()).isEqualTo("TEST123");
        assertThat(portfolioDetails.date()).isEqualTo("15-Jun-2023");
        assertThat(portfolioDetails.xirr()).isEqualTo(15.0); // XIRR is multiplied by 100

        verify(navService).getNavByDateWithRetry(123456L, asOfDate);
        verify(xIRRCalculatorService).calculateXIRRBySchemeId(123456L, 789L, asOfDate);
    }

    @Test
    @DisplayName("Should handle NavNotFoundException and use default NAV of 10")
    void shouldHandleNavNotFoundExceptionAndUseDefaultNav() throws NavNotFoundException {
        // Given
        String panNumber = "ABCDE1234F";
        LocalDate asOfDate = LocalDate.of(2023, 6, 15);

        PortfolioDetailsProjection projection = mock(PortfolioDetailsProjection.class);
        when(projection.getSchemeId()).thenReturn(123456L);
        when(projection.getSchemeName()).thenReturn("NFO Scheme");
        when(projection.getFolioNumber()).thenReturn("NFO123");
        when(projection.getBalanceUnits()).thenReturn(50.0);
        when(projection.getSchemeDetailId()).thenReturn(999L);

        List<PortfolioDetailsProjection> projections = List.of(projection);
        when(userCASDetailsService.getPortfolioDetailsByPanAndAsOfDate(panNumber, asOfDate))
                .thenReturn(projections);

        when(navService.getNavByDateWithRetry(123456L, asOfDate))
                .thenThrow(new NavNotFoundException("NAV not found for NFO scheme", asOfDate));

        when(xIRRCalculatorService.calculateXIRRBySchemeId(123456L, 999L, asOfDate))
                .thenReturn(0.0);

        // When
        List<PortfolioDetailsDTO> result =
                portfolioServiceHelper.getPortfolioDetailsByPANAndAsOfDate(panNumber, asOfDate);

        // Then
        assertThat(result).hasSize(1);
        PortfolioDetailsDTO portfolioDetails = result.get(0);
        assertThat(portfolioDetails.totalValue()).isEqualTo(500.0); // 50 * 10 (default NAV)
        assertThat(portfolioDetails.schemeName()).isEqualTo("NFO Scheme");
        assertThat(portfolioDetails.folioNumber()).isEqualTo("NFO123");
        assertThat(portfolioDetails.date()).isEqualTo("2023-06-15");
        assertThat(portfolioDetails.xirr()).isEqualTo(0.0);

        verify(navService).getNavByDateWithRetry(123456L, asOfDate);
        verify(xIRRCalculatorService).calculateXIRRBySchemeId(123456L, 999L, asOfDate);
    }

    private UserSchemeDTO createUserSchemeDTO(int transactionCount) {
        List<UserTransactionDTO> transactions = new ArrayList<>();
        for (int i = 0; i < transactionCount; i++) {
            transactions.add(mock(UserTransactionDTO.class));
        }

        UserSchemeDTO scheme = mock(UserSchemeDTO.class);
        when(scheme.transactions()).thenReturn(transactions);
        return scheme;
    }

    // Test class for ObjectMapper testing
    public static class TestClass {
        private final String name;

        public TestClass(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestClass testClass = (TestClass) obj;
            return name != null ? name.equals(testClass.name) : testClass.name == null;
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }
}
