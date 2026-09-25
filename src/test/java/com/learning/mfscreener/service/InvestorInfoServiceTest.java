package com.learning.mfscreener.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.learning.mfscreener.repository.InvestorInfoEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvestorInfoServiceTest {

    @Mock
    private InvestorInfoEntityRepository investorInfoEntityRepository;

    private InvestorInfoService investorInfoService;

    @BeforeEach
    void setUp() {
        investorInfoService = new InvestorInfoService(investorInfoEntityRepository);
    }

    @Test
    @DisplayName("Should return true when investor exists by email and name")
    void shouldReturnTrueWhenInvestorExistsByEmailAndName() {
        // Given
        String email = "test@example.com";
        String name = "John Doe";
        when(investorInfoEntityRepository.existsByEmailAndName(email, name)).thenReturn(true);

        // When
        boolean result = investorInfoService.existsByEmailAndName(email, name);

        // Then
        assertThat(result).isTrue();
        verify(investorInfoEntityRepository).existsByEmailAndName(email, name);
    }

    @Test
    @DisplayName("Should return false when investor does not exist by email and name")
    void shouldReturnFalseWhenInvestorDoesNotExistByEmailAndName() {
        // Given
        String email = "nonexistent@example.com";
        String name = "Jane Smith";
        when(investorInfoEntityRepository.existsByEmailAndName(email, name)).thenReturn(false);

        // When
        boolean result = investorInfoService.existsByEmailAndName(email, name);

        // Then
        assertThat(result).isFalse();
        verify(investorInfoEntityRepository).existsByEmailAndName(email, name);
    }

    @Test
    @DisplayName("Should handle null email gracefully")
    void shouldHandleNullEmailGracefully() {
        // Given
        String email = null;
        String name = "John Doe";
        when(investorInfoEntityRepository.existsByEmailAndName(email, name)).thenReturn(false);

        // When
        boolean result = investorInfoService.existsByEmailAndName(email, name);

        // Then
        assertThat(result).isFalse();
        verify(investorInfoEntityRepository).existsByEmailAndName(email, name);
    }

    @Test
    @DisplayName("Should handle null name gracefully")
    void shouldHandleNullNameGracefully() {
        // Given
        String email = "test@example.com";
        String name = null;
        when(investorInfoEntityRepository.existsByEmailAndName(email, name)).thenReturn(false);

        // When
        boolean result = investorInfoService.existsByEmailAndName(email, name);

        // Then
        assertThat(result).isFalse();
        verify(investorInfoEntityRepository).existsByEmailAndName(email, name);
    }

    @Test
    @DisplayName("Should handle both email and name as null")
    void shouldHandleBothEmailAndNameAsNull() {
        // Given
        String email = null;
        String name = null;
        when(investorInfoEntityRepository.existsByEmailAndName(email, name)).thenReturn(false);

        // When
        boolean result = investorInfoService.existsByEmailAndName(email, name);

        // Then
        assertThat(result).isFalse();
        verify(investorInfoEntityRepository).existsByEmailAndName(email, name);
    }

    @Test
    @DisplayName("Should handle empty email string")
    void shouldHandleEmptyEmailString() {
        // Given
        String email = "";
        String name = "John Doe";
        when(investorInfoEntityRepository.existsByEmailAndName(email, name)).thenReturn(false);

        // When
        boolean result = investorInfoService.existsByEmailAndName(email, name);

        // Then
        assertThat(result).isFalse();
        verify(investorInfoEntityRepository).existsByEmailAndName(email, name);
    }

    @Test
    @DisplayName("Should handle empty name string")
    void shouldHandleEmptyNameString() {
        // Given
        String email = "test@example.com";
        String name = "";
        when(investorInfoEntityRepository.existsByEmailAndName(email, name)).thenReturn(false);

        // When
        boolean result = investorInfoService.existsByEmailAndName(email, name);

        // Then
        assertThat(result).isFalse();
        verify(investorInfoEntityRepository).existsByEmailAndName(email, name);
    }

    @Test
    @DisplayName("Should handle whitespace-only email")
    void shouldHandleWhitespaceOnlyEmail() {
        // Given
        String email = "   ";
        String name = "John Doe";
        when(investorInfoEntityRepository.existsByEmailAndName(email, name)).thenReturn(false);

        // When
        boolean result = investorInfoService.existsByEmailAndName(email, name);

        // Then
        assertThat(result).isFalse();
        verify(investorInfoEntityRepository).existsByEmailAndName(email, name);
    }

    @Test
    @DisplayName("Should handle whitespace-only name")
    void shouldHandleWhitespaceOnlyName() {
        // Given
        String email = "test@example.com";
        String name = "   ";
        when(investorInfoEntityRepository.existsByEmailAndName(email, name)).thenReturn(false);

        // When
        boolean result = investorInfoService.existsByEmailAndName(email, name);

        // Then
        assertThat(result).isFalse();
        verify(investorInfoEntityRepository).existsByEmailAndName(email, name);
    }

    @Test
    @DisplayName("Should work with valid email formats")
    void shouldWorkWithValidEmailFormats() {
        // Given
        String[] validEmails = {"user@domain.com", "user.name@domain.co.uk", "user+tag@domain.org", "123@domain.net"};
        String name = "Valid User";

        for (String email : validEmails) {
            when(investorInfoEntityRepository.existsByEmailAndName(email, name)).thenReturn(true);

            // When
            boolean result = investorInfoService.existsByEmailAndName(email, name);

            // Then
            assertThat(result).isTrue();
            verify(investorInfoEntityRepository).existsByEmailAndName(email, name);
        }
    }
}
