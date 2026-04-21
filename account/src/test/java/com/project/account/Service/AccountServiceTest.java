package com.project.account.Service;

import com.project.account.Entity.Account;
import com.project.account.Enum.Status;
import com.project.account.Exception.InsufficientBalanceException;
import com.project.account.Repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;


    @Test
    void shouldCreateAccount() {

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(i -> i.getArgument(0));

        Account acc = accountService.createAccount("Bishwajit");

        assertNotNull(acc);
        assertEquals("Bishwajit", acc.getCustomerName());
        assertEquals(BigDecimal.ZERO, acc.getBalance());
    }

    @Test
    void shouldReturnAccount() {

        Account acc = new Account();
        acc.setAccountNumber("123");

        when(accountRepository.findByAccountNumber("123"))
                .thenReturn(Optional.of(acc));

        Optional<Account> result = accountRepository.findByAccountNumber("123");

        assertEquals("123", result.get().getAccountNumber());
    }

    @Test
    void shouldDebitSuccessfully() throws AccountNotFoundException {

        Account acc = new Account();
        acc.setAccountNumber("123");
        acc.setBalance(new BigDecimal("100"));
        acc.setStatus(Status.ACTIVE);

        when(accountRepository.findByAccountNumber("123"))
                .thenReturn(Optional.of(acc));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(i -> i.getArgument(0));

        Account result = accountService.updateBalance("123", new BigDecimal("50"), true);

        assertEquals(new BigDecimal("50"), result.getBalance());
    }

    @Test
    void shouldFailWhenInsufficientBalance() {

        Account acc = new Account();
        acc.setBalance(new BigDecimal("10"));
        acc.setStatus(Status.ACTIVE);

        when(accountRepository.findByAccountNumber("123"))
                .thenReturn(Optional.of(acc));

        assertThrows(InsufficientBalanceException.class, () -> {
            accountService.updateBalance("123", new BigDecimal("50"), true);
        });
    }

    @Test
    void concurrentWithdrawTest() throws InterruptedException {

        Account acc = new Account();
        acc.setAccountNumber("123");
        acc.setBalance(new BigDecimal("100"));
        acc.setStatus(Status.ACTIVE);

        when(accountRepository.findByAccountNumber("123")).thenReturn(Optional.of(acc));

        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    accountService.updateBalance("123", new BigDecimal("10"), true);
                } catch (Exception ignored) {}
            });
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        assertTrue(acc.getBalance().compareTo(BigDecimal.ZERO) >= 0);
    }
}
