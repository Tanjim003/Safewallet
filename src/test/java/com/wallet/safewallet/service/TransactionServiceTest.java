package com.wallet.safewallet.service;

import com.wallet.safewallet.dto.TransactionHistoryItem;
import com.wallet.safewallet.entity.User;
import com.wallet.safewallet.entity.Wallet;
import com.wallet.safewallet.repository.TransactionRepository;
import com.wallet.safewallet.repository.UserRepository;
import com.wallet.safewallet.repository.WalletRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private TransactionService transactionService;

    private MockedStatic<SecurityContextHolder> securityContextHolderMock;

    @BeforeEach
    void setUp() {
        securityContextHolderMock = mockStatic(SecurityContextHolder.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        when(SecurityContextHolder.getContext()).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("01712345678");
    }

    @AfterEach
    void tearDown() {
        securityContextHolderMock.close();
    }

    @Test
    void shouldReturnTransactionHistoryForLoggedInUser() {
        // Arrange
        String phone = "01712345678";
        Long userId = 1L;
        Long walletId = 10L;

        User user = new User();
        user.setId(userId);
        user.setPhone(phone);

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setUserId(userId);

        PageRequest pageRequest = PageRequest.of(0, 10);

        TransactionHistoryItem item = new TransactionHistoryItem(
                1L, "01712345678", "01884353431",
                BigDecimal.valueOf(500), "TRANSFER", "SUCCESS", "test",
                LocalDateTime.now()
        );
        List<TransactionHistoryItem> items = List.of(item);
        Page<TransactionHistoryItem> expectedPage = new PageImpl<>(items, pageRequest, 1);

        when(userRepository.findByPhone(phone)).thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        // Use Pageable.class to match the exact parameter type
        when(transactionRepository.findTransactionHistoryByWalletId(eq(walletId), any(Pageable.class)))
                .thenReturn(expectedPage);

        // Act
        Page<TransactionHistoryItem> result = transactionService.getTransactionHistory(0, 10);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals(item.getTransactionId(), result.getContent().get(0).getTransactionId());
        verify(userRepository).findByPhone(phone);
        verify(walletRepository).findByUserId(userId);
        // Verify with explicit Pageable matcher
        verify(transactionRepository).findTransactionHistoryByWalletId(eq(walletId), any(Pageable.class));

      }
    }
